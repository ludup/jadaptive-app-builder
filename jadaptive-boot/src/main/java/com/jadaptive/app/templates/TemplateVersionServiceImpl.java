package com.jadaptive.app.templates;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ConfigHelper;
import com.jadaptive.api.app.ResourcePackage;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.events.Events;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.IncludeView;
import com.jadaptive.api.template.Index;
import com.jadaptive.api.template.NoOp;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectExtension;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.ObjectTemplateType;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.UniqueIndex;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;
import com.jadaptive.api.template.ViewPermission;
import com.jadaptive.api.templates.JsonTemplateEnabledService;
import com.jadaptive.api.templates.TemplateVersion;
import com.jadaptive.api.templates.TemplateVersionRepository;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.app.entity.MongoEntity;
import com.jadaptive.app.json.ObjectMapperHolder;
import com.jadaptive.utils.Utils;
import com.jadaptive.utils.Version;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;

@Service
public class TemplateVersionServiceImpl extends AbstractLoggingServiceImpl implements TemplateVersionService  {
	
	@Autowired
	protected TemplateVersionRepository versionRepository; 
	
	@Autowired
	protected ObjectMapperHolder objectMapper;
	
	@Autowired
	private PluginManager pluginManager; 
	
	@Autowired
	private ObjectTemplateRepository templateRepository;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private ClassLoaderService classService;
	
	@Autowired
	private ObjectService objectService; 
	
	private Map<String,ObjectTemplate> loadedTemplates = new HashMap<>();
	private Map<String,Class<? extends ObjectEvent<?>>> eventClasses = new HashMap<>();

	@Override
	public Iterable<TemplateVersion> list() throws RepositoryException, ObjectException {
		return versionRepository.list();
	}
	
	@Override
	public <E extends AbstractUUIDEntity> void processTemplates(Tenant tenant, JsonTemplateEnabledService<E> templateEnabledService) {
		
		if(log.isInfoEnabled()) {
			log.info("Processing templates for {} in {}", 
					templateEnabledService.getResourceKey(),
					tenant.getName());
		}
		
		try {
			
			Collection<PathInfo> orderedTemplates = findVersionedTemplates(
					buildTemplatePaths(tenant, templateEnabledService));
			
			Set<String> resourceKeys = new HashSet<>();
			for(PathInfo template : orderedTemplates) {
				
				String filename = template.path.getFileName().toString().substring(0, template.path.getFileName().toString().length()-5);
				String[] elements = filename.split("_");
				if(elements.length != 2) {
					throw new IOException("Template json file should be named <resourceKey>_<version>.json");
				}
				Version version = new Version(elements[1]);
				String templateResourceKey = elements[0];
				resourceKeys.add(templateResourceKey);
				
				String uuid = templateEnabledService.getResourceKey() + "_" + templateResourceKey;
				Version currentVersion = versionRepository.getCurrentVersion(uuid);
				
				if(currentVersion==null || version.compareTo(currentVersion) > 0) {
					if(versionRepository.hasProcessed(uuid, version.toString())) {
						if(log.isInfoEnabled()) {
							log.info("Already processed {}", template.path.getFileName());
						}
						continue;
					}
					
					processTemplate(template, uuid, templateEnabledService, version);
				}
				
			}

			if(log.isInfoEnabled()) {
				log.info("Finished processing templates for {}", templateEnabledService.getResourceKey());
			}
			
		} catch(IOException | RepositoryException e) {
			log.error("Template repository failure!", e);
		}
		
	}


	private List<PathInfo> buildTemplatePaths(Tenant tenant, JsonTemplateEnabledService<?> templateEnabledService) throws IOException {
		
		List<PathInfo> paths = new ArrayList<>();
		
		if(log.isInfoEnabled()) {
			log.info("Looking for {} templates in {}", templateEnabledService.getTemplateFolder(), tenant.getName());
		}
		
		if(!templateEnabledService.isSystemOnly()) {
			File sharedConf = new File(ConfigHelper.getSharedFolder(), templateEnabledService.getTemplateFolder());
			
			if(sharedConf.exists()) {
				paths.add(new PathInfo(sharedConf.toPath()));
			}
			
			for(ResourcePackage pkg : ConfigHelper.getSharedPackages()) {
				paths.add(new PathInfo(pkg, templateEnabledService.getTemplateFolder()));
			}
			
			addClasspathResources("system/shared/" + templateEnabledService.getTemplateFolder(), paths);
			
			if(!tenant.isSystem()) {

				File tenantConf = new File(ConfigHelper.getTenantsFolder(), tenant.getDomain());
				File templateConf = new File(tenantConf, templateEnabledService.getTemplateFolder());
				if(templateConf.exists()) {
					paths.add(new PathInfo(templateConf.toPath()));
				}
				for(ResourcePackage pkg : ConfigHelper.getTenantPackages(tenant)) {
					paths.add(new PathInfo(pkg, templateEnabledService.getTemplateFolder()));
				}
			} else {
				File prvConf = new File(ConfigHelper.getSystemPrivateFolder(), templateEnabledService.getTemplateFolder());
				if(prvConf.exists()) {
					paths.add(new PathInfo(prvConf.toPath()));
				}
				for(ResourcePackage pkg : ConfigHelper.getSystemPrivatePackages()) {
					paths.add(new PathInfo(pkg, templateEnabledService.getTemplateFolder()));
				}
				
				addClasspathResources("system/private/" + templateEnabledService.getTemplateFolder(), paths);
			}
			
		} else {
			File systemConf = ConfigHelper.getSystemSubFolder(templateEnabledService.getTemplateFolder());
			if(systemConf.exists()) {
				paths.add(new PathInfo(systemConf.toPath()));
			}
			for(ResourcePackage pkg : ConfigHelper.getTenantPackages(tenant)) {
				paths.add(new PathInfo(pkg, templateEnabledService.getTemplateFolder()));
			}
		}
		
		return paths;
	}
		
	private void addClasspathResources(String path, List<PathInfo> paths) throws IOException {
		
		Map<String, String> env = new HashMap<>(); 
        env.put("create", "true");
        
		for(PluginWrapper w : pluginManager.getPlugins()) {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(w.getPluginClassLoader());
			Resource[] resources = resolver.getResources("classpath*:" + path);
			for(Resource resource : resources) {
				try {
					URI uri = resource.getURL().toURI();
					if(!uri.getScheme().equals("file")) {
						try {
							FileSystems.getFileSystem(uri);
						} catch(FileSystemNotFoundException e) { 
							FileSystems.newFileSystem(uri, env);
						}
					}
					paths.add(new PathInfo(Paths.get(uri)));
				} catch (URISyntaxException e) {
				}
			}
		}
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
		Resource[] resources = resolver.getResources("classpath*:" + path);
		for(Resource resource : resources) {
			try {
				URI uri = resource.getURL().toURI();
				if(!uri.getScheme().equals("file")) {
					try {
						FileSystems.getFileSystem(uri);
					} catch(FileSystemNotFoundException e) { 
						FileSystems.newFileSystem(uri, env);
					}
				}
				paths.add(new PathInfo(Paths.get(uri)));
			} catch (URISyntaxException e) {
			}
		}
	}

	protected Collection<PathInfo> findVersionedTemplates(List<PathInfo> paths) throws IOException {
		
		List<PathInfo> orderedTemplates = new ArrayList<>();
		
		for(PathInfo path : paths) {
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Searching for templates folder in %s", 
						path.path.toString()));
			}

			if(Files.exists(path.getPath())) {
				Files.list(path.getPath())
				.filter(f -> f.getFileName().toString().endsWith(".json"))
				.forEach(jsonFile -> {
					if(log.isInfoEnabled()) {
						log.info("Found {}", jsonFile.toString());
					}
					orderedTemplates.add(new PathInfo(jsonFile));
				});
			}
		}
	
		
		Collections.<PathInfo>sort(orderedTemplates, new Comparator<PathInfo>() {
			
			@Override
			public int compare(PathInfo o1, PathInfo o2) {
				return o1.getFilename().toString().compareTo(o2.getFilename().toString());
			}
		});
		
		return orderedTemplates;
	}

	@SuppressWarnings("unchecked")
	private <E extends AbstractUUIDEntity> void processTemplate(PathInfo resource, String resourceKey, JsonTemplateEnabledService<E> repository, Version version) {
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Processing template {} resource {} on tenant {}", 
						resourceKey, version.toString(),
						tenantService.getCurrentTenant().getName());
			}
			
			List<E> objects = objectMapper.getObjectMapper().readValue(
					Files.newInputStream(resource.path), 
					objectMapper.getObjectMapper().getTypeFactory().constructCollectionType(List.class, repository.getResourceClass()));
			
			repository.saveTemplateObjects(objects, new TransactionAdapter<E>() {

				@Override
				public void afterSave(E object) throws RepositoryException, ObjectException {
					TemplateVersion  t = new TemplateVersion();
					t.setUuid(resourceKey);
					t.setVersion(version.toString());
					versionRepository.save(t);
					
					log.info("Created {} {} '{}' version {}",
							repository.getResourceClass().getSimpleName(), 
							repository.getResourceKey(), 
							object.getUuid(), 
							version.toString());
				}
			});

		} catch (Throwable e) {
			log.error(String.format("Failed to process template %s", resourceKey), e);
			System.exit(0);
		}
	}
	
	class PathInfo {
		
		ResourcePackage pkg;
		Path path;
		
		PathInfo(ResourcePackage pkg, String path) {
			this.pkg = pkg;
			this.path = pkg.resolvePath(path);
		}
		
		PathInfo(Path path) {
			this.path = path;
		}

		public boolean isPackage() {
			return Objects.nonNull(pkg);
		}
		
		public ResourcePackage getPkg() {
			return pkg;
		}

		public Path getPath() {
			return path;
		}
		
		public String getFilename() {
			return Objects.nonNull(pkg) ? pkg.getFilename() : path.getFileName().toString();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerAnnotatedTemplates(boolean newSchema) {
			
		for(PluginWrapper w : pluginManager.getPlugins()) {

			if(log.isInfoEnabled()) {
				log.info("Scanning plugin {} for entity templates in {}", 
						w.getPluginId(),
						w.getPlugin().getClass().getPackage().getName());
			}

			if(w.getPlugin()==null) {
				continue;
			}

            try (ScanResult scanResult =
                    new ClassGraph()                 
                        .enableAllInfo()  
                        .addClassLoader(w.getPluginClassLoader())
                        .whitelistPackages(w.getPlugin().getClass().getPackage().getName())   
                        .scan()) {                  
                for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(ObjectDefinition.class.getName())) {

                    if(classInfo.getPackageName().startsWith(w.getPlugin().getClass().getPackage().getName())) {
                        if(log.isInfoEnabled()) {
    						log.info("Found template {}", classInfo.getName());
    					}
                    	registerAnnotatedTemplate((Class<? extends UUIDDocument>) classInfo.loadClass(), newSchema);
                    }
                }
            }
		}
		
		try (ScanResult scanResult =
                new ClassGraph()                 
                    .enableAllInfo()  
                    .addClassLoader(getClass().getClassLoader())
                    .whitelistPackages("com.jadaptive")   
                    .scan()) {                  
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(ObjectDefinition.class.getName())) {
                if(log.isInfoEnabled()) {
					log.info("Found template {}", classInfo.getName());
				}
                registerAnnotatedTemplate((Class<? extends UUIDDocument>) classInfo.loadClass(), newSchema);
            }
        }
	}

	@SuppressWarnings("unchecked")
	@Override
	public ObjectTemplate registerAnnotatedTemplate(Class<? extends UUIDDocument> clz, boolean newSchema) {
		
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Registering template from annotations on class {}", clz.getSimpleName());
			}
			
			List<Class<?>> parents = new ArrayList<>();
			Class<?> c = clz.getSuperclass();
			while(Objects.nonNull(c)) {
				
				ObjectDefinition e = c.getAnnotation(ObjectDefinition.class);
				
				if(Objects.nonNull(e)) {
					parents.add(c);
				}
				c = c.getSuperclass();
			}
			
			if(!parents.isEmpty()) {
				Collections.reverse(parents);
				for(Class<?> parent : parents) {
					registerAnnotatedTemplate((Class<? extends UUIDDocument>) parent, newSchema);
				}
			}
			
			ObjectDefinition e = clz.getAnnotation(ObjectDefinition.class);
			
			if(Objects.isNull(e.resourceKey())) {
				log.error("{} requires resource key in ObjectDefinition annotation!", clz.getSimpleName());
				throw new IllegalStateException();
			}
			
			ObjectTemplate template = loadedTemplates.get(e.resourceKey());
			if(Objects.nonNull(template)) {
				return template;
			}
			
			try {
				template = templateRepository.get(e.resourceKey());
			} catch (ObjectException ee) {
				template = new ObjectTemplate();
				template.setUuid(e.resourceKey());
			}
			
			ObjectDefinition parent = getParentTemplate(clz);
			ObjectDefinition collection = getCollectionTemplate(clz);
			
			boolean auditObject = ReflectionUtils.hasAnnotation(clz, AuditedObject.class);
			boolean generateEventTemplates = ReflectionUtils.hasAnnotation(clz, GenerateEventTemplates.class);
			
			if(Objects.nonNull(parent)) {
				if(log.isInfoEnabled()) {
					log.info("{} template has {} as parent", e.resourceKey(), parent.resourceKey());
				}
				template.setParentTemplate(parent.resourceKey());
				ObjectTemplate parentTemplate = templateRepository.get(parent.resourceKey());
				if(e.templateType()!=ObjectTemplateType.EXTENDED) {
					if(!parentTemplate.getChildTemplates().contains(e.resourceKey())) {
						parentTemplate.addChildTemplate(e.resourceKey());
					}
				}
				
				templateRepository.saveOrUpdate(parentTemplate);
			}
			
			if(Objects.nonNull(collection)) {
				template.setCollectionKey(collection.resourceKey());
			} else {
				template.setCollectionKey(e.resourceKey());
			}
			template.setDisplayKey(getDisplayKey(clz, e.resourceKey()));
			template.setResourceKey(e.resourceKey());
			template.setTemplateType(e.templateType());
			template.setBundle(StringUtils.isBlank(e.bundle()) ? e.resourceKey() : e.bundle());
			template.setName(e.resourceKey());
			template.setHidden(e.hidden());
			template.setSystem(!auditObject && e.system());
			template.setType(e.type());
			template.setScope(e.scope());
			template.getFields().clear();
			template.setTemplateClass(clz.getName());
			template.getAliases().clear();
			template.getAliases().addAll(Arrays.asList(e.aliases()));
			template.setDefaultFilter(e.defaultFilter());
			template.setDefaultColumn(e.defaultColumn());
			template.setName(e.resourceKey());
			
			template.setCreatable(e.creatable());
			template.setUpdatable(e.updatable());
			template.setDeletable(e.deletable());
			template.setPermissionProtected(e.requiresPermission());
			
			String nameField = "uuid";
			
			
			List<Field> fields = new ArrayList<>();
			resolveFields(clz, fields, e.recurse());
			
			for(Field field :fields) {
				
				ObjectField objectAnnotation = field.getAnnotation(ObjectField.class);
				
				if(Objects.nonNull(objectAnnotation)) {
					FieldTemplate t = processFieldAnnotations(objectAnnotation, field, /*i18n,*/ template);
					if(objectAnnotation.nameField()) {
						nameField =t.getResourceKey();
					}
					template.getFields().add(t);
				}
			}

			template.setNameField(nameField);
			templateRepository.saveOrUpdate(template);
			loadedTemplates.put(e.resourceKey(), template);
			templateService.registerTemplateClass(e.resourceKey(), clz, template);
			
			switch(template.getType()) {
			case COLLECTION:
			case SINGLETON:
				if(template.getPermissionProtected() && !template.hasParent()) {
					permissionService.registerStandardPermissions(template.getResourceKey());
				}
				break;
			default:
				// Embedded objects do not have direct permissions
			}
			
			registerIndexes(template, clz, newSchema);
			
			if(generateEventTemplates) {
				generateEventTemplates(template, clz, clz.getAnnotation(GenerateEventTemplates.class).value(), newSchema);
			}
			
			return template;
			
		} catch(RepositoryException | ObjectException e) {
			log.error("Failed to process annotated template {}", clz.getSimpleName(), e);
			throw new IllegalStateException();
		}
	}
	
	@Override
	public Class<? extends ObjectEvent<?>> getEventClass(String resourceKey) {
		return eventClasses.get(resourceKey);
	}
	
	private void generateEventTemplates(ObjectTemplate template, Class<?> clz, String group, boolean newSchema) {
	
		if(log.isInfoEnabled()) {
			log.info("Generating events for {}", template.getResourceKey());
		}
		generateEventTemplate(StringUtils.capitalize(group) + "Created",
				template.getResourceKey(), template.getBundle(), group, clz, 
				Events.created(template.getResourceKey()), newSchema, true);
		
		generateEventTemplate(StringUtils.capitalize(group) + "Updated", 
				template.getResourceKey(), template.getBundle(), group, clz, 
				Events.updated(template.getResourceKey()), newSchema, true);
		
		generateEventTemplate(StringUtils.capitalize(group) + "Deleted",
				template.getResourceKey(), template.getBundle(), group, clz, 
				Events.deleted(template.getResourceKey()), newSchema, true);
		
		
		generateEventTemplate(StringUtils.capitalize(group) + "Creating",
				template.getResourceKey(), template.getBundle(), group, clz, 
				Events.creating(template.getResourceKey()), newSchema, false);
		
		generateEventTemplate(StringUtils.capitalize(group) + "Updating", 
				template.getResourceKey(), template.getBundle(), group, clz, 
				Events.updating(template.getResourceKey()), newSchema, false);
		
		generateEventTemplate(StringUtils.capitalize(group) + "Deleting",
				template.getResourceKey(), template.getBundle(), group, clz, 
				Events.deleting(template.getResourceKey()), newSchema, false);
		
	}
	
	@SuppressWarnings("unchecked")
	private void generateEventTemplate(String className, String resourceKey, String bundle, String group, Class<?> clz, 
			String eventKey, boolean newSchema, boolean audited) {
		
		Generic genericType = TypeDescription.Generic.Builder.parameterizedType(ObjectEvent.class, clz).build();
		
		if(log.isInfoEnabled()) {
			log.info("Generating event templates and class for {}", clz.getSimpleName());
		}
		try {
			
			var b = new ByteBuddy().subclass(genericType, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
					  .name(String.format("com.jadaptive.events.%s.%s", group, className));
			
			
			 b = b.annotateType(AnnotationDescription.Builder.ofType(ObjectDefinition.class)
	                  .define("resourceKey", eventKey)
	                  .define("scope", ObjectScope.GLOBAL)
	                  .define("type", ObjectType.OBJECT)
	                  .define("templateType", ObjectTemplateType.EVENT)
	                  .define("updatable", false)
	                  .define("deletable", false)
	                  .define("creatable", false)
	                  .define("bundle", bundle)
	                  .build());
			
			 if(audited) {
				 b = b.annotateType(AnnotationDescription.Builder.ofType(AuditedObject.class).build());
			 }
			 
			 b = b.annotateType(clz.getAnnotation(ObjectViews.class))
			  .defineConstructor(Visibility.PUBLIC)
			  .withParameters(clz)
			  .intercept(MethodCall
			               .invoke(ObjectEvent.class.getDeclaredConstructor(String.class, String.class))
			               .onSuper().with(eventKey, group)
			               .andThen(FieldAccessor.ofField("object").setsArgumentAt(0)))
			  .defineField("object", clz, Visibility.PRIVATE)
			  .annotateField(AnnotationDescription.Builder.ofType(ObjectField.class)
					  .define("type", FieldType.OBJECT_EMBEDDED).build())
			  .annotateField(AnnotationDescription.Builder.ofType(Validator.class)
					  .define("type", ValidationType.RESOURCE_KEY)
					  .define("value", resourceKey).build())
			  .defineMethod("getObject", clz, Visibility.PUBLIC)
	          .intercept(FieldAccessor.ofField("object"))
	          .defineConstructor(Visibility.PUBLIC)
			  .withParameters(clz, Throwable.class)
			  .intercept(MethodCall
			               .invoke(ObjectEvent.class.getDeclaredConstructor(String.class, String.class, Throwable.class))
			               .onSuper().with(eventKey, group).withArgument(1)
			               .andThen(FieldAccessor.ofField("object").setsArgumentAt(0)));
			 
			var dynamicType = b.make()
					  .load(classService.getClassLoader())
					  .getLoaded();
			
			eventClasses.put(eventKey, (Class<? extends ObjectEvent<?>>) dynamicType);
			registerAnnotatedTemplate((Class<? extends UUIDDocument>) dynamicType, newSchema);
			
		} catch (SecurityException | NoSuchMethodException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

	}

	@Override
	public AbstractObject extendWith(AbstractObject baseObject, 
			ObjectTemplate extensionTemplate, Collection<String> extensions) {
		
		Class<? extends UUIDDocument> baseClass = templateService.getTemplateClass(baseObject.getResourceKey());
		ObjectTemplate baseTemplate = templateService.get(baseObject.getResourceKey());
		
		if(baseTemplate.isExtended()) {
			baseClass = templateService.getTemplateClass(baseTemplate.getParentTemplate());
			baseTemplate = templateService.get(baseTemplate.getParentTemplate());
		}
		
		if(!extensions.isEmpty()) {
			String packageName = "com.jadaptive.extensions";
			ObjectTemplate t =  templateService.getBaseTemplate(baseTemplate);
			String templateName = t.getResourceKey();
			
			String className = WordUtils.capitalize(String.format("%s_%s",
					templateName, Utils.generateRandomAlphaNumericString(16)));
			
			String resourceKey = WordUtils.uncapitalize(className);
	
			Builder<? extends UUIDDocument> b = new ByteBuddy().subclass(baseClass, 
					ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
					  .name(String.format("%s.%s", packageName, className));
			
			b = b.annotateType(AnnotationDescription.Builder.ofType(ObjectDefinition.class)
	                 .define("resourceKey", resourceKey)
	                 .define("scope", baseTemplate.getScope())
	                 .define("type", baseTemplate.getType())
	                 .define("templateType", ObjectTemplateType.EXTENDED)
	                 .define("updatable", baseTemplate.isUpdatable())
	                 .define("deletable", baseTemplate.isDeletable())
	                 .define("creatable", baseTemplate.isDeletable())
	                 .define("bundle", baseTemplate.getBundle())
	                 .build());
			 
			 b = b.method(ElementMatchers.named("getResourceKey"))
		                .intercept(FixedValue.value(resourceKey));
	//		}
			for(String extension : extensions) {
				 
				Class<?> ext = templateService.getTemplateClass(extension);
				ObjectExtension e = ext.getAnnotation(ObjectExtension.class);
				
				if(!e.extendingInterface().equals(NoOp.class)) {
					b = b.implement(e.extendingInterface());
				}
				
				String getter = "get" + ext.getSimpleName();
				String setter = "set" + ext.getSimpleName();
				String field = WordUtils.uncapitalize(ext.getSimpleName());
				
				b = b.defineField(field, ext, Visibility.PRIVATE)
				  .annotateField(AnnotationDescription.Builder.ofType(ObjectField.class)
						  .define("type", FieldType.OBJECT_EMBEDDED).build())
				  .annotateField(AnnotationDescription.Builder.ofType(Validator.class)
						  .define("type", ValidationType.RESOURCE_KEY)
						  .define("value", templateService.getTemplateResourceKey(ext)).build())
				  .defineMethod(setter, Void.TYPE, Visibility.PUBLIC)
					  	 .withParameters(ext)
					  	 .intercept(FieldAccessor.ofField(field))
				  .defineMethod(getter, ext, Visibility.PUBLIC)
				  		.intercept(FieldAccessor.ofField(field));
			
			}
			
			var type = b.make().load(classService.getClassLoader());
			
			var clz = type.getLoaded();
			
			ObjectTemplate template = registerAnnotatedTemplate(clz, false);
			template.setExtensions(extensions);
			template.setClassDefinition(Base64.getEncoder().encodeToString(type.getBytes()));
			templateService.saveOrUpdate(template);

			Document doc = new Document(baseObject.getDocument());
			
			doc.put("_clz", clz.getName());
			doc.put("resourceKey", resourceKey);
			
			AbstractObject obj =  new MongoEntity(doc);
			
			return obj;
		} else {
			
			Document doc = new Document(baseObject.getDocument());
			
			doc.put("_clz", baseClass.getName());
			doc.put("resourceKey", baseTemplate.getResourceKey());
			
			AbstractObject obj =  new MongoEntity(doc);
			
			return obj;
		}
		
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void loadExtendedTemplates(Tenant tenant) {
		
		for(ObjectTemplate template : templateRepository.list(
				SearchField.eq("templateType", ObjectTemplateType.EXTENDED.name()))) {
			if(!classService.hasTemplateClass(template)) {
				classService.injectClass(template);
			}
			try {
				templateService.registerTemplateClass(template.getResourceKey(), 
						(Class<? extends UUIDDocument>) classService.findClass(template.getTemplateClass()),
						template);
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException();
			}
		}
	}

	@Override
	public void registerTenantIndexes(boolean newSchema) {
		
		for(ObjectTemplate template : loadedTemplates.values()) {
			if(!template.isSystem()) {
				registerIndexes(template, templateService.getTemplateClass(template.getResourceKey()), newSchema);
			}
		}
 	}
	
	private void registerIndexes(ObjectTemplate template, Class<?> clz, boolean newSchema) {
		
		Index[] nonUnique = clz.getAnnotationsByType(Index.class);
		UniqueIndex[] unique = clz.getAnnotationsByType(UniqueIndex.class);
		
		switch(template.getType()) {
		case COLLECTION:
		case SINGLETON:
			if(!template.hasParent()) {
				templateRepository.createIndexes(template, nonUnique, unique, newSchema);
			}
			break;
		default:
			// Embedded objects do not have direct permissions
		}
		
	}

	private FieldTemplate processFieldAnnotations(ObjectField field, /*Field parentField,*/ Field f, /*Properties i18n,*/ ObjectTemplate template) {

		FieldTemplate t = new FieldTemplate();
		t.setResourceKey(f.getName());
		t.setParentKey(template.getUuid());
		t.setFormVariable(StringUtils.isNotBlank(field.formVariable()) ? field.formVariable() : f.getName());
		t.setDefaultValue(field.defaultValue());
		t.setFieldType(selectFieldType(f.getType(), field.type()));
		t.setHidden(field.hidden());
		t.setSystem(false);
		t.setSearchable(field.searchable());
		t.setTextIndex(field.textIndex());
		t.setUnique(field.unique());
		t.setMeta(field.meta());
		t.setCollection(f.getType().isAssignableFrom(Collection.class));
		t.setReadOnly(field.readOnly());
		t.setAlternativeId(field.alternativeId());
		t.setManuallyEncrypted(field.manualEncryption());
		t.setAutomaticallyEncrypted(field.automaticEncryption());
		
		switch(field.type()) {
		case OBJECT_EMBEDDED:
		{
			Class<?> clz = ReflectionUtils.getObjectType(f);
			ObjectDefinition objd = clz.getAnnotation(ObjectDefinition.class);
			if(Objects.nonNull(objd)) {
				t.getValidators().add(new FieldValidator(
						ValidationType.RESOURCE_KEY, 
						objd.resourceKey(), ObjectTemplate.RESOURCE_KEY, "resourceKey.invalid"));
			}
			t.getValidators().add(new FieldValidator(
					ValidationType.OBJECT_TYPE, 
					f.getType().getName(), ObjectTemplate.RESOURCE_KEY, "objectType.invalid"));
			break;
		}
		case ENUM:
		{
			String resourceKey = field.references();
			if(StringUtils.isBlank(resourceKey)) {
				Class<?> clz = f.getType();
				if(Collection.class.isAssignableFrom(clz)) {
					clz = (Class<?>)((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
				}
				resourceKey = clz.getName();
			}
			t.getValidators().add(new FieldValidator(
					ValidationType.OBJECT_TYPE, 
					f.getType().getName(),
					ObjectTemplate.RESOURCE_KEY,
					"objectType.invalid"));
			t.getValidators().add(new FieldValidator(
					ValidationType.RESOURCE_KEY, 
					resourceKey,
					ObjectTemplate.RESOURCE_KEY,
					"resourceKey.invalid"));
			break;
		}
		case OBJECT_REFERENCE:
		{
//			if(isEvent) {
//				t.setFieldType(FieldType.TEXT);
//			} else {
				if(StringUtils.isBlank(field.references())) {
					throw new IllegalStateException("Missing references attribute from OBJECT_REFERENCE typed @ObjectField annotation");
				} else {
					t.getValidators().add(new FieldValidator(
							ValidationType.OBJECT_TYPE, 
							field.references(), 
							ObjectTemplate.RESOURCE_KEY,
							"objectType.invalid"));
					t.getValidators().add(new FieldValidator(
							ValidationType.RESOURCE_KEY, 
							field.references(),
							ObjectTemplate.RESOURCE_KEY,
							"resourceKey.invalid"));
				}
//			}
			break;
		}
		default:
			break;
		}
		
		Validator[] validators = f.getAnnotationsByType(Validator.class);
		if(Objects.nonNull(validators)) {
			for(Validator validator : validators) {
				t.getValidators().add(new FieldValidator(validator.type(), validator.value(), 
						StringUtils.firstNonBlank(validator.bundle(), template.getBundle()), validator.i18n()));
			}
		}

		IncludeView included = f.getAnnotation(IncludeView.class);
		if(Objects.nonNull(included)) {
			t.getViews().addAll(Arrays.asList(included.values()));
		} else {
			t.getViews().addAll(Arrays.asList(FieldView.values()));
		}
		
		ExcludeView excluded = f.getAnnotation(ExcludeView.class);
		if(Objects.nonNull(excluded)) {
			t.getViews().removeAll(Arrays.asList(excluded.values()));
		}
		
		ViewPermission permissions = f.getAnnotation(ViewPermission.class);
		if(Objects.nonNull(permissions)) {
			t.getViewPermissions().addAll(Arrays.asList(permissions.values()));
			t.setRequireAllPermissions(permissions.requireAll());
		}
		
		return t;
	}

	@SuppressWarnings("unused")
	private Collection<String> verifyColumnNames(ObjectTemplate template, Collection<String> columns) {
		Map<String,FieldTemplate> definedColumns = template.toMap();
		for(String column : columns) {
			if(!definedColumns.containsKey(column)) {
				throw new IllegalArgumentException(String.format("%s is not a valid column name", column));
			}
		}
		return columns;
	}
	
	private ObjectDefinition getParentTemplate(Class<?> clz) {
		
		Class<?> parent = clz.getSuperclass();
		ObjectDefinition template = null;
		while(parent!=null && template==null){
			
			ObjectDefinition t = parent.getAnnotation(ObjectDefinition.class);
			if(Objects.nonNull(t)) {
				template = t;
			}
			parent = parent.getSuperclass();
		}
		
		return template;
	}
	
	private String getDisplayKey(Class<?> clz, String defaultValue) {
		
		Class<?> parent = clz.getSuperclass();
		String displayKey = defaultValue;
		while(parent!=null){
			
			ObjectDefinition t = parent.getAnnotation(ObjectDefinition.class);
			if(Objects.nonNull(t) && t.templateType()!=ObjectTemplateType.EXTENDED) {
				displayKey = t.resourceKey();
				break;
			}
			parent = parent.getSuperclass();
		}
		
		return displayKey;
	}
	
	private ObjectDefinition getCollectionTemplate(Class<?> clz) {
		
		Class<?> parent = clz.getSuperclass();
		ObjectDefinition template = null;
		while(parent!=null){
			
			ObjectDefinition t = parent.getAnnotation(ObjectDefinition.class);
			if(Objects.nonNull(t)) {
				template = t;
			}
			parent = parent.getSuperclass();
		}
		
		return template;
	}

	private void resolveFields(Class<?> clz, List<Field> fields, boolean recurse) {
		
		if(recurse) {
			if(!clz.getSuperclass().equals(Object.class)) {
				resolveFields(clz.getSuperclass(), fields, true);
			}
		}
		
		for(Field field : clz.getDeclaredFields()) {
			fields.add(field);
		}
	}

	private FieldType selectFieldType(Class<?> type, FieldType declaredType) {
		if(Objects.nonNull(declaredType)) {
			return declaredType;
		}
		
		if(Long.class.equals(type) || long.class.equals(type)) {
			return FieldType.LONG;
		} else if(Integer.class.equals(type) || int.class.equals(type)) {
			return FieldType.INTEGER;
		} else if(Double.class.equals(type) || double.class.equals(type)) {
			return FieldType.DECIMAL;
		} else if(Boolean.class.equals(type) || boolean.class.equals(type)) {
			return FieldType.BOOL;
		} else if(Date.class.equals(type)) {
			return FieldType.TIMESTAMP;
		} else if(String.class.equals(type)) {
			return FieldType.TEXT;
		} else if(Enum.class.isAssignableFrom(type)) {
			return FieldType.ENUM;
		}
		
		throw new IllegalStateException(String.format("Could not detemine field type of class %s", type.getSimpleName()));
	}

	@Override
	public void rebuildReferences() {
		
		log.info("Rebuilding references for {}", tenantService.getCurrentTenant().getName());
		Set<ObjectTemplate> processed = new HashSet<>();
		for(ObjectTemplate template : templateService.allCollectionTemplates()) {
			while(template.hasParent()) {
				template = templateService.get(template.getParentTemplate());
			}
			if(!processed.contains(template)) {
				objectService.rebuildReferences(template);
			}
			processed.add(template);
		}
	}
}

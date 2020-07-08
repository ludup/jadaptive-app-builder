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

import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ConfigHelper;
import com.jadaptive.api.app.ResourcePackage;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.IncludeView;
import com.jadaptive.api.template.Index;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
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
import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.app.json.ObjectMapperHolder;
import com.jadaptive.utils.Version;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

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
	
	@Override
	public Iterable<TemplateVersion> list() throws RepositoryException, ObjectException {
		return versionRepository.list();
	}
	
	@Override
	public <E extends AbstractUUIDEntity> void processTemplates(Tenant tenant, JsonTemplateEnabledService<E> templateEnabledService) {
		
		if(log.isInfoEnabled()) {
			log.info("Processing templates for {}", templateEnabledService.getResourceKey());
		}
		
		try {
			
			Collection<PathInfo> orderedTemplates = findVersionedTemplates(
					buildTemplatePaths(tenant, templateEnabledService));
			
			Set<String> resourceKeys = new HashSet<>();
			for(PathInfo template : orderedTemplates) {
				
				String filename = template.path.getFileName().toString().substring(0, template.path.getFileName().toString().length()-5);
				String[] elements = filename.split("_");
				if(elements.length != 2) {
					throw new IOException("Template json file should be named <id>_<version>.json");
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
				log.info("Processing template {} resource {}", resourceKey, version.toString());
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

	@Override
	public void registerAnnotatedTemplates() {
			
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
                    	registerAnnotatedTemplate(classInfo.loadClass());
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
                registerAnnotatedTemplate(classInfo.loadClass());
            }
        }
	}

	private void registerAnnotatedTemplate(Class<?> clz) {
		
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Registering template from annotations on class {}", clz.getSimpleName());
			}
			
			ObjectDefinition e = clz.getAnnotation(ObjectDefinition.class);
			
			ObjectTemplate template;
			try {
				template = templateRepository.get(e.resourceKey());
			} catch (ObjectException ee) {
				template = new ObjectTemplate();
				template.setUuid(e.resourceKey());
			}
			
			ObjectDefinition parent = getParentTemplate(clz);
			if(Objects.nonNull(parent)) {
				if(log.isInfoEnabled()) {
					log.info("{} template has {} as parent", e.resourceKey(), parent.resourceKey());
				}
				template.setParentTemplate(parent.resourceKey());
			}
			
//			Properties i18n = new Properties();
//			i18n.setProperty(String.format("%s.name", template.getResourceKey()), e.name());
			
			template.setResourceKey(e.resourceKey());
			template.setName(e.resourceKey());
			template.setHidden(e.hidden());
			template.setSystem(e.system());
			template.setType(e.type());
			template.setScope(e.scope());
			template.getFields().clear();
			template.setTemplateClass(clz.getName());
			template.getAliases().clear();
			template.getAliases().addAll(Arrays.asList(e.aliases()));
			template.setDefaultFilter(e.defaultFilter());
			template.setName(e.resourceKey());
			
			Index[] nonUnique = clz.getAnnotationsByType(Index.class);
			UniqueIndex[] unique = clz.getAnnotationsByType(UniqueIndex.class);
			
			List<Field> fields = new ArrayList<>();
			resolveFields(clz, fields, e.recurse());
			
			
			for(Field field :fields) {
				
				ObjectField objectAnnotation = field.getAnnotation(ObjectField.class);
				
				if(Objects.nonNull(objectAnnotation)) {
					
					FieldTemplate t = processFieldAnnotations(objectAnnotation, field, /*i18n,*/ template);
					template.getFields().add(t);
				}
			}

			templateRepository.saveOrUpdate(template);
			
//			File i18nFolder = new File("i18n");
//			
//			if(clz.getClassLoader() instanceof PluginClassLoader) {
//				try {
//					Field desc = PluginClassLoader.class.getDeclaredField("pluginDescriptor");
//					desc.setAccessible(true);
//					PluginDescriptor d = (PluginDescriptor) desc.get(clz.getClassLoader());
//					i18nFolder = new File(i18nFolder, d.getPluginId());
//				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//
//			} else {
//				i18nFolder = new File(i18nFolder, "jadaptive-boot");
//			}
//			
//			i18nFolder.mkdirs();
//			try(OutputStream out = new FileOutputStream(new File(i18nFolder, 
//					String.format("%s.properties", template.getResourceKey())))) {
//				i18n.store(out, "# Generated by TemplateVersionServiceImpl");
//			} catch (IOException e1) {
//				log.error("Failed to create template i18n", e);
//			}
			
			switch(template.getType()) {
			case COLLECTION:
			case SINGLETON:
				templateRepository.createIndexes(template, nonUnique, unique);
				permissionService.registerStandardPermissions(template.getResourceKey());
				break;
			default:
				// Embedded objects do not have direct permissions
			}
			
		} catch(RepositoryException | ObjectException e) {
			log.error("Failed to process annotated template {}", clz.getSimpleName(), e);
		}
	}
	
	private FieldTemplate processFieldAnnotations(ObjectField field, Field f, /*Properties i18n,*/ ObjectTemplate template) {
//		i18n.setProperty(String.format("%s.%s.name", template.getResourceKey(), f.getName()), field.name());
//		i18n.setProperty(String.format("%s.%s.desc", template.getResourceKey(), f.getName()), field.description());
		
		FieldTemplate t = new FieldTemplate();
		t.setResourceKey(f.getName());
		t.setDefaultValue(field.defaultValue());
		t.setFieldType(selectFieldType(f.getType(), field.type()));
		t.setHidden(field.hidden());
		t.setRequired(field.required());
		t.setSystem(false);
		t.setSearchable(field.searchable());
		t.setTextIndex(field.textIndex());
		t.setUnique(field.unique());
		t.setCollection(f.getType().isAssignableFrom(Collection.class));
		t.setReadOnly(field.readOnly());
		t.setAlternativeId(field.alternativeId());
		
//		t.setDescription(field.description());
//		t.setName(field.name());
		
		
		switch(field.type()) {
		case OBJECT_EMBEDDED:
		{
			Class<?> clz = ReflectionUtils.getObjectType(f);

			t.getValidators().add(new FieldValidator(
					ValidationType.RESOURCE_KEY, 
					clz.getAnnotation(ObjectDefinition.class).resourceKey()));
			t.getValidators().add(new FieldValidator(
					ValidationType.OBJECT_TYPE, 
					f.getType().getName()));
			break;
		}
		case ENUM:
		case OBJECT_REFERENCE:
		{
			String resourceKey = field.references();
			if(StringUtils.isBlank(resourceKey)) {
				Class<?> clz = f.getType();
				if(Collections.class.isAssignableFrom(clz)) {
					clz = (Class<?>)((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
				}
				resourceKey = clz.getName();
			}
			if(StringUtils.isBlank(field.references())) {
				t.getValidators().add(new FieldValidator(
						ValidationType.OBJECT_TYPE, 
						f.getType().getName()));
				t.getValidators().add(new FieldValidator(
						ValidationType.RESOURCE_KEY, 
						resourceKey));
				templateService.registerObjectDependency(resourceKey, template);
			} else {
				t.getValidators().add(new FieldValidator(
						ValidationType.OBJECT_TYPE, 
						field.references()));
				t.getValidators().add(new FieldValidator(
						ValidationType.RESOURCE_KEY, 
						field.references()));
				templateService.registerObjectDependency(field.references(), template);
			}
			break;
		}
		default:
			break;
		}
		
		Validator[] validators = f.getAnnotationsByType(Validator.class);
		if(Objects.nonNull(validators)) {
			for(Validator validator : validators) {
				t.getValidators().add(new FieldValidator(validator.type(), validator.value()));
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
		
		for(Field field : clz.getDeclaredFields()) {
			fields.add(field);
		}
		if(recurse) {
			if(!clz.getSuperclass().equals(Object.class)) {
				resolveFields(clz.getSuperclass(), fields, true);
			}
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
}

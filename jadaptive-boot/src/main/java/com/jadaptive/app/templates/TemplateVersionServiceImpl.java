package com.jadaptive.app.templates;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
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
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateRepository;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.Index;
import com.jadaptive.api.template.Table;
import com.jadaptive.api.template.Template;
import com.jadaptive.api.template.UniqueIndex;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.templates.TemplateEnabledService;
import com.jadaptive.api.templates.TemplateVersion;
import com.jadaptive.api.templates.TemplateVersionRepository;
import com.jadaptive.api.templates.TemplateVersionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.app.db.DocumentHelper;
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
	private EntityTemplateRepository templateRepository;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public Collection<TemplateVersion> list() throws RepositoryException, EntityException {
		return versionRepository.list();
	}
	
	@Override
	public <E extends AbstractUUIDEntity> void processTemplates(Tenant tenant, TemplateEnabledService<E> templateEnabledService) {
		
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


	private List<PathInfo> buildTemplatePaths(Tenant tenant, TemplateEnabledService<?> templateEnabledService) throws IOException {
		
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
			
			if(!tenant.getSystem()) {

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
	private <E extends AbstractUUIDEntity> void processTemplate(PathInfo resource, String resourceKey, TemplateEnabledService<E> repository, Version version) {
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Processing template {} resource {}", resourceKey, version.toString());
			}
			
			List<E> objects = objectMapper.getObjectMapper().readValue(
					Files.newInputStream(resource.path), 
					objectMapper.getObjectMapper().getTypeFactory().constructCollectionType(List.class, repository.getResourceClass()));
			
			repository.saveTemplateObjects(objects, new TransactionAdapter<E>() {

				@Override
				public void afterSave(E object) throws RepositoryException, EntityException {
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
                for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(Template.class.getName())) {

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
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(Template.class.getName())) {
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
			
			Template e = clz.getAnnotation(Template.class);
			
			if(e.resourceKey().equals("builtinUsers")) {
				System.out.println();
			}

			
			EntityTemplate template;
			try {
				template = templateRepository.get(clz.getSimpleName());
			} catch (EntityException ee) {
				template = new EntityTemplate();
				template.setUuid(clz.getSimpleName());
			}
			
			Template parent = getParentTemplate(clz);
			if(Objects.nonNull(parent)) {
				if(log.isInfoEnabled()) {
					log.info("{} template has {} as parent", e.resourceKey(), parent.resourceKey());
				}
				template.setParentTemplate(parent.resourceKey());
			}
			
			template.setResourceKey(e.resourceKey());
			template.setHidden(e.hidden());
			template.setSystem(e.system());
			template.setName(e.name());
			template.setType(e.type());
			template.getFields().clear();
			template.setTemplateClass(clz.getName());
			template.getAliases().clear();
			template.getAliases().addAll(Arrays.asList(e.aliases()));
			
			Index[] nonUnique = clz.getAnnotationsByType(Index.class);
			UniqueIndex[] unique = clz.getAnnotationsByType(UniqueIndex.class);
			
			List<Field> fields = new ArrayList<>();
			resolveFields(clz, fields, e.recurse());
			
			for(Field f :fields) {
				
				Column[] annotations = f.getAnnotationsByType(Column.class);
				
				if(Objects.nonNull(annotations) && annotations.length > 0) {
					
					Column field = annotations[0];
					FieldTemplate t = new FieldTemplate();
					t.setResourceKey(f.getName());
					t.setDefaultValue(field.defaultValue());
					t.setDescription(field.description());
					t.setFieldType(selectFieldType(f.getType(), field.type()));
					t.setHidden(field.hidden());
					t.setName(field.name());
					t.setRequired(field.required());
					t.setSystem(false);
					t.setSearchable(field.searchable());
					t.setTextIndex(field.textIndex());
					t.setUnique(field.unique());
					t.setCollection(f.getType().isAssignableFrom(Collection.class));
					t.setReadOnly(field.readOnly());
					
					switch(field.type()) {
					case ENUM:
					case OBJECT_EMBEDDED:
					case OBJECT_REFERENCE:
						if(StringUtils.isBlank(field.references())) {
							t.getValidators().add(new FieldValidator(
									ValidationType.OBJECT_TYPE, 
									f.getType().getName()));
							t.getValidators().add(new FieldValidator(
									ValidationType.RESOURCE_KEY, 
									DocumentHelper.getTemplateResourceKey(f.getType())));
						} else {
							t.getValidators().add(new FieldValidator(
									ValidationType.OBJECT_TYPE, 
									field.references()));
							t.getValidators().add(new FieldValidator(
									ValidationType.RESOURCE_KEY, 
									field.references()));
						}
						break;
					default:
						break;
					}
					
					template.getFields().add(t);
				}
			}
			
			Table table = clz.getAnnotation(Table.class);
			if(Objects.nonNull(table)) {
				;
				template.setDefaultColumns(verifyColumnNames(template, Arrays.asList(table.defaultColumns())));
				template.setOptionalColumns(verifyColumnNames(template, Arrays.asList(table.optionalColumns())));
			}
			templateRepository.saveOrUpdate(template);
			
			switch(template.getType()) {
			case COLLECTION:
			case SINGLETON:
				templateRepository.createIndexes(template, nonUnique, unique);
				permissionService.registerStandardPermissions(template.getResourceKey());
				break;
			default:
				// Embedded objects do not have direct permissions
			}
			
		} catch(RepositoryException | EntityException e) {
			log.error("Failed to process annotated template {}", clz.getSimpleName(), e);
		}
	}
	
	private Collection<String> verifyColumnNames(EntityTemplate template, Collection<String> columns) {
		Map<String,FieldTemplate> definedColumns = template.toMap();
		for(String column : columns) {
			if(!definedColumns.containsKey(column)) {
				throw new IllegalArgumentException(String.format("%s is not a valid column name", column));
			}
		}
		return columns;
	}
	
	private Template getParentTemplate(Class<?> clz) {
		
		Class<?> parent = clz.getSuperclass();
		Template template = null;
		while(parent!=null){
			
			Template t = parent.getAnnotation(Template.class);
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

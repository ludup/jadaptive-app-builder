package com.jadaptive.app.entity.template;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectExtension;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.TemplateView;
import com.jadaptive.api.template.TemplateViewField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.templates.JsonTemplateEnabledService;
import com.jadaptive.api.templates.SystemTemplates;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.app.db.DocumentHelper;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

@Service
public class TemplateServiceImpl extends AuthenticatedService implements TemplateService, JsonTemplateEnabledService<ObjectTemplate>, TenantAware {
	
	static Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);
	
	@Autowired
	private ObjectTemplateRepository repository; 

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ClassLoaderService classService;
	
	@Autowired
	private PluginManager pluginManager;
	
//	Map<String,List<ObjectTemplate>> objectForwardDependencies = new HashMap<>();
//	Map<String,List<String>> objectReverseDependencies = new HashMap<>();
	Map<String,Class<? extends UUIDDocument>> templateClazzes = new HashMap<>();
	Map<Class<?>,String> templateResourceKeys = new HashMap<>();
	
	private Map<String,Collection<Class<? extends UUIDDocument>>> extensionClasses = new HashMap<>();
	private Map<String,Set<String>> extensionsByTemplate = new HashMap<>();
	
	@Override
	public void registerTemplateClass(String resourceKey, Class<? extends UUIDDocument> templateClazz, ObjectTemplate template) {
		for(String alias : template.getAliases()) {
			templateClazzes.put(alias, templateClazz);
		}
		templateClazzes.put(resourceKey, templateClazz);
		templateResourceKeys.put(templateClazz, resourceKey);
	}
	
//	@Override
//	public void registerObjectDependency(String resourceKey, ObjectTemplate template) {
//		if(!objectForwardDependencies.containsKey(resourceKey)) {
//			objectForwardDependencies.put(resourceKey, new ArrayList<>());
//		}
//		List<ObjectTemplate> depends = objectForwardDependencies.get(resourceKey);
//		depends.add(template);
//		registerReverseDependency(template.getResourceKey(), resourceKey);
//	}
//	
//	private void registerReverseDependency(String resourceKey, String template) {
//		if(!objectReverseDependencies.containsKey(resourceKey)) {
//			objectReverseDependencies.put(resourceKey, new ArrayList<>());
//		}
//		List<String> depends = objectReverseDependencies.get(resourceKey);
//		depends.add(template);
//	}
//	
	
	@Override
	public ObjectTemplate get(String resourceKey) throws RepositoryException, ObjectException {
		
		ObjectTemplate e = repository.get(SearchField.or(
				SearchField.eq("resourceKey", resourceKey),
				SearchField.in("aliases", resourceKey)));
		
		if(Objects.isNull(e)) {
			throw new ObjectException(String.format("Cannot find entity with resource key %s", resourceKey));
		}
		
		return e;
	}

	@Override
	public Iterable<ObjectTemplate> list() throws RepositoryException, ObjectException {
		
		permissionService.assertRead(ObjectTemplate.RESOURCE_KEY);
		
		return repository.list();
	}
	
	@SuppressWarnings("unchecked")
	private void generateExtensionTemplates() {
		
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
                for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(ObjectExtension.class.getName())) {

                    if(classInfo.getPackageName().startsWith(w.getPlugin().getClass().getPackage().getName())) {
                        if(log.isInfoEnabled()) {
    						log.info("Found extension {}", classInfo.getName());
    					}
                        registerExtension((Class<? extends UUIDDocument>) classInfo.loadClass());
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
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(ObjectExtension.class.getName())) {
                if(log.isInfoEnabled()) {
					log.info("Found extension {}", classInfo.getName());
				}
                registerExtension((Class<? extends UUIDDocument>) classInfo.loadClass());
            }
        }
		
	}
	private void registerExtension(Class<? extends UUIDDocument> loadClass) {
		
		ObjectExtension e = loadClass.getAnnotation(ObjectExtension.class);
		ObjectDefinition d = loadClass.getAnnotation(ObjectDefinition.class);
		if(!extensionClasses.containsKey(e.extend())) {
			extensionClasses.put(e.extend(), new ArrayList<>());
			extensionsByTemplate.put(e.extend(), new TreeSet<>());
		}
		extensionClasses.get(e.extend()).add(loadClass);
		extensionsByTemplate.get(e.extend()).add(d.resourceKey());
	}
	
	@Override
	public Collection<String> getTemplateExtensions(ObjectTemplate template) {
		
		if(extensionsByTemplate.isEmpty()) {
			generateExtensionTemplates();
		}
		
		Collection<String> tmp = extensionsByTemplate.get(template.getParentTemplate());
		var results = new TreeSet<String>();
		
		if(Objects.nonNull(tmp) && !tmp.isEmpty()) {
		
			results.addAll(extensionsByTemplate.get(template.getParentTemplate()));
			
			for(FieldTemplate field : template.getFields()) {
				if(field.getFieldType()==FieldType.OBJECT_EMBEDDED) {
					results.remove(field.getValidationValue(ValidationType.RESOURCE_KEY));
				}
			}
		}
		
		return Collections.unmodifiableCollection(results);

	}
	
	
	@Override
	public Iterable<ObjectTemplate> children(String uuid) {
		return repository.list(SearchField.eq("parentTemplate", uuid));
	}
	

	@Override
	public Iterable<ObjectTemplate> singletons() throws RepositoryException, ObjectException {
		return repository.list(SearchField.eq("type", ObjectType.SINGLETON.name()));
	}
	
	@Override
	public Collection<ObjectTemplate> table(String searchField, String searchValue, int start, int length, SortOrder order, String sortField) throws RepositoryException, ObjectException {
		
		permissionService.assertRead(ObjectTemplate.RESOURCE_KEY);
		
		return repository.table(searchField, searchValue, start, length, order, sortField);
	}
	
	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public void saveOrUpdate(ObjectTemplate template) throws RepositoryException, ObjectException {
		
		permissionService.assertWrite(ObjectTemplate.RESOURCE_KEY);
		
		repository.saveOrUpdate(template);
		
	}
	
	@Override
	public Integer getTemplateOrder() {
		return SystemTemplates.ENTITY_TEMPLATE.ordinal();
	}

	@Override
	public Class<ObjectTemplate> getResourceClass() {
		return ObjectTemplate.class;
	}

	@Override
	public String getName() {
		return "EntityTemplate";
	}
	
	@Override
	public void saveTemplateObjects(List<ObjectTemplate> objects, @SuppressWarnings("unchecked") TransactionAdapter<ObjectTemplate>... ops) throws RepositoryException, ObjectException {
		for(ObjectTemplate obj : objects) {
			if(validateTemplate(obj)) {
				saveOrUpdate(obj);
				switch(obj.getType()) {
				case COLLECTION:
				case SINGLETON:
					if(obj.getPermissionProtected()) {
						permissionService.registerStandardPermissions(obj.getResourceKey());
					}
					break;
				default:
					// Embedded objects do not have direct permissions
				}
				for(TransactionAdapter<ObjectTemplate> op : ops) {
					op.afterSave(obj);
				}
			}
		}
	}

	private boolean validateTemplate(ObjectTemplate obj) {
		
		if(!Objects.isNull(obj.getFields())) {
			for(FieldTemplate t : obj.getFields()) {
				switch(t.getFieldType()) {
				case OBJECT_EMBEDDED:
				case OBJECT_REFERENCE:
					if(!validatorPresent(t, ValidationType.OBJECT_TYPE)) {
						throw new IllegalStateException(String.format("Missing OBJECT_TYPE validator on field %s", t.getResourceKey()));
					}
					break;
				default:
					break;
				}
			}
		}
		
		return true;
	}

	private boolean validatorPresent(FieldTemplate field, ValidationType validator) {
		for(FieldValidator v : field.getValidators()) {
			if(v.getType() == validator) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getResourceKey() {
		return ObjectTemplate.RESOURCE_KEY;
	}

	@Override
	public boolean isSystemOnly() {
		return true;
	}

	@Override
	public String getTemplateFolder() {
		return "templates";
	}
	
	@Override
	public <T extends UUIDEntity> T createObject(Map<String,Object> values, Class<T> baseClass) throws ParseException {
		return DocumentHelper.convertDocumentToObject(baseClass, new Document(values));
	}

	@Override
	public Iterable<ObjectTemplate> getTemplatesWithScope(ObjectScope scope) {
		return repository.list(SearchField.eq("scope", scope.name()));
	}

	@Override
	public void initializeSystem(boolean newSchema) {
		initializeTenant(getCurrentTenant(), newSchema);
	}

	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {
		
//		if(log.isInfoEnabled()) {
//			log.info("Object dependencies");
//		}
//		for(Entry<String,List<String>> e : objectReverseDependencies.entrySet()) {
//			log.info(String.format(" %s", e.getKey()));
//			for(String t : e.getValue()) {
//				log.info(String.format(" - %s", t));
//			}
//		}
		
	}

	@Override
	public List<TemplateView> getViews(ObjectTemplate template, boolean disableViews) {
		
		if(StringUtils.isNotBlank(template.getTemplateClass())) {
			return getAnnotatedViews(template, disableViews);
		}
		return getDynamicViews(template);
	}

	private List<TemplateView> getDynamicViews(ObjectTemplate template) {
		return null;
	}

	private List<TemplateView> getAnnotatedViews(ObjectTemplate template, boolean disableViews) {
		try {

			Class<?> clz = templateClazzes.get(template.getResourceKey());
			if(Objects.isNull(clz)) {
				clz = classService.findClass(template.getTemplateClass());
			}
			Map<String, TemplateView> views = new HashMap<>();
			Map<String,String> childViews = new HashMap<>();
			views.put(null, new TemplateView(template.getBundle()));
		
			if(!disableViews) {
				iterateClassHeirarchy(clz, views, new HashSet<>());
			}
			
			processFields(null,template, clz, views, new LinkedList<>(), disableViews); 

			for(String child : childViews.keySet()) {
				TemplateView view = views.remove(child);
				views.get(view.getParent()).addChildView(view);
			}
			
			List<TemplateView> results = new ArrayList<>(views.values());
			Collections.sort(results, new Comparator<TemplateView>() {
				@Override
				public int compare(TemplateView o1, TemplateView o2) {
					return o1.getWeight().compareTo(o2.getWeight());
				}
			});
			return results;
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	private void iterateClassHeirarchy(Class<?> clz, Map<String, TemplateView> views, Set<Class<?>> processed) {
		
		Class<?> tmp = clz;
		
		do {
			
			if(processed.contains(tmp)) {
				tmp = tmp.getSuperclass();
				continue;
			}
			
			ObjectViews annonatedViews = tmp.getAnnotation(ObjectViews.class);

			if(Objects.nonNull(annonatedViews)) {
				for(ObjectViewDefinition def : annonatedViews.value()) {
					views.put(def.value(), new TemplateView(def));
				}
			}
			
			processed.add(tmp);
			
			for(Field field : tmp.getDeclaredFields()) {
				if(UUIDEntity.class.isAssignableFrom(field.getType())) {
					iterateClassHeirarchy(field.getType(), views, processed);
				}
			}

			tmp = tmp.getSuperclass();
		
		} while(Objects.nonNull(tmp));
	
	}

	@Override
	public SortOrder getTableSortOrder(ObjectTemplate template) {
	
		Class<?> clz = getTemplateClass(template.getResourceKey());
		
		TableView view = ReflectionUtils.getAnnotation(clz, TableView.class);
		if(Objects.nonNull(view)) {
			return view.sortOrder();
		}
		return SortOrder.DESC;
		
	}
	
	@Override
	public String getTableSortField(ObjectTemplate template) {
	
		Class<?> clz = getTemplateClass(template.getResourceKey());
		
		TableView view = ReflectionUtils.getAnnotation(clz, TableView.class);
		if(Objects.nonNull(view)) {
			return view.sortField();
		}
		String val = template.getNameField();
		if(val.equals("uuid")) {
			return "_id";
		}
		return val;
		
	}
	
	@Override
	public FieldRenderer getRenderer(FieldTemplate field, ObjectTemplate template) {
		
		try {
			Class<?> clz = getTemplateClass(template.getResourceKey());
			if(Objects.isNull(clz)) {
				log.warn("Template class for {} appears to be missing", template.getResourceKey());
				return FieldRenderer.DEFAULT;
			}
			Field f = ReflectionUtils.getField(clz, field.getResourceKey());
			ObjectView v = f.getAnnotation(ObjectView.class);
			if(Objects.nonNull(v)) {
				return v.renderer() == null ? FieldRenderer.DEFAULT : v.renderer();
			}
		} catch (NoSuchFieldException e) {
		}
		
		return FieldRenderer.DEFAULT;
	}
	
	private void processFields(ObjectView currentView, ObjectTemplate template, Class<?> clz, Map<String, TemplateView> views, LinkedList<FieldTemplate> objectPath, boolean disableViews) throws NoSuchFieldException, ClassNotFoundException {
		
		for(FieldTemplate field : template.getFields()) {
			
			Field f = ReflectionUtils.getField(clz, field.getResourceKey());
			ObjectView v = f.getAnnotation(ObjectView.class);
			
			if(field.getFieldType()==FieldType.OBJECT_EMBEDDED && !field.getCollection()) {
				Class<?> c = ReflectionUtils.getObjectType(f);
				String resourceKey = field.getValidationValue(ValidationType.RESOURCE_KEY);
				ObjectTemplate ct = get(resourceKey);
				
				LinkedList<FieldTemplate> currentPath = new LinkedList<>();
				if(!objectPath.isEmpty()) {
					currentPath.addAll(objectPath);
				}
				currentPath.add(field);
				processFields(v, ct, c, views, currentPath, disableViews);
				continue;
			}
			
			if(Objects.isNull(v)) { 
				TemplateView o = views.get(!disableViews && Objects.nonNull(currentView) ? currentView.value() : null);
				if(Objects.isNull(o)) {
					o = new TemplateView(template.getBundle(), currentView.value());
					views.put(currentView.value(), o);
				}
				o.addField(new TemplateViewField(null, o, field, objectPath));				
			} else if(StringUtils.isBlank(v.value())) {
				TemplateView o = views.get(!disableViews && Objects.nonNull(currentView) ? currentView.value() : null);
				if(Objects.isNull(o)) {
					o = new TemplateView(template.getBundle());
					views.put(null, o);
				}
				o.addField(new TemplateViewField(v, o, field, objectPath));
			} else {
				TemplateView o = views.get(disableViews ? null : v.value());
				if(Objects.isNull(o)) {
					o = new TemplateView(template.getBundle(), v.value());
					views.put(v.value(), o);
				}
				o.addField(new TemplateViewField(v, o, field, objectPath));
			}
		}
	}

	@Override
	public Iterable<ObjectTemplate> allCollectionTemplates() {
		return repository.list(SearchField.eq("type", ObjectType.COLLECTION.name()));
	}

	@Override
	public Class<? extends UUIDDocument> getTemplateClass(String resourceKey) {
		return templateClazzes.get(resourceKey);
	}
	
	@Override
	public String getTemplateResourceKey(Class<?> clz) {
		return templateResourceKeys.get(clz);
	}
	
	@Override
	public String getTemplateResourceKey(String clz) {
		try {
			return getTemplateResourceKey(classService.getClassLoader().loadClass(clz));
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(String.format("Missing template for class %s", clz));
		}
	}

	@Override
	public void delete(ObjectTemplate objectTemplate) {
		
		repository.delete(objectTemplate);
		templateClazzes.remove(objectTemplate.getResourceKey());
		try {
			templateResourceKeys.remove(classService.getClassLoader().loadClass(objectTemplate.getTemplateClass()));
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(String.format("Missing template for class %s", objectTemplate.getTemplateClass()));
		}
	}

	@Override
	public ObjectTemplate getParentTemplate(ObjectTemplate template) {
		while(template.hasParent()) {
			template = get(template.getParentTemplate());
		}
		return template;
	}
}
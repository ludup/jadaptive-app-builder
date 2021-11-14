package com.jadaptive.app.entity.template;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.OrderedField;
import com.jadaptive.api.template.OrderedView;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.templates.JsonTemplateEnabledService;
import com.jadaptive.api.templates.SystemTemplates;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.app.db.DocumentHelper;

@Service
public class TemplateServiceImpl extends AuthenticatedService implements TemplateService, JsonTemplateEnabledService<ObjectTemplate>, TenantAware {
	
	static Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);
	
	@Autowired
	private ObjectTemplateRepository repository; 
	
	@Autowired
	private ObjectService entityService;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ClassLoaderService classService;
	
	Map<String,List<ObjectTemplate>> objectForwardDependencies = new HashMap<>();
	Map<String,List<String>> objectReverseDependencies = new HashMap<>();
	Map<String,Class<?>> templateClazzes = new HashMap<>();
	Map<Class<?>,String> templateResourceKeys = new HashMap<>();
	
	@Override
	public void registerTemplateClass(String resourceKey, Class<?> templateClazz) {
		templateClazzes.put(resourceKey, templateClazz);
		templateResourceKeys.put(templateClazz, resourceKey);
	}
	
	@Override
	public void registerObjectDependency(String resourceKey, ObjectTemplate template) {
		if(!objectForwardDependencies.containsKey(resourceKey)) {
			objectForwardDependencies.put(resourceKey, new ArrayList<>());
		}
		List<ObjectTemplate> depends = objectForwardDependencies.get(resourceKey);
		depends.add(template);
		registerReverseDependency(template.getResourceKey(), resourceKey);
	}
	
	private void registerReverseDependency(String resourceKey, String template) {
		if(!objectReverseDependencies.containsKey(resourceKey)) {
			objectReverseDependencies.put(resourceKey, new ArrayList<>());
		}
		List<String> depends = objectReverseDependencies.get(resourceKey);
		depends.add(template);
	}
	
	
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
	
	@Override
	public Iterable<ObjectTemplate> children(String uuid) {
		return repository.list(SearchField.eq("parentTemplate", uuid));
	}
	

	@Override
	public Iterable<ObjectTemplate> singletons() throws RepositoryException, ObjectException {
		return repository.list(SearchField.eq("type", ObjectType.SINGLETON.name()));
	}
	
	@Override
	public Collection<ObjectTemplate> table(String searchField, String searchValue, String order, int start, int length) throws RepositoryException, ObjectException {
		
		permissionService.assertRead(ObjectTemplate.RESOURCE_KEY);
		
		return repository.table(searchField, searchValue, order, start, length);
	}
	
	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public void saveOrUpdate(ObjectTemplate template) throws RepositoryException, ObjectException {
		
		permissionService.assertReadWrite(ObjectTemplate.RESOURCE_KEY);
		
		repository.saveOrUpdate(template);
		
	}

	@Override
	public void delete(String uuid) throws ObjectException {
		
		permissionService.assertReadWrite(ObjectTemplate.RESOURCE_KEY);
		
		entityService.deleteAll(uuid);
		repository.delete(uuid);
		
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
						log.error(String.format("Missing OBJECT_TYPE validator on field %s", t.getResourceKey()));
						return false;
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
		return false;
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
		
		if(log.isInfoEnabled()) {
			log.info("Object dependencies");
		}
		for(Entry<String,List<String>> e : objectReverseDependencies.entrySet()) {
			log.info(String.format(" %s", e.getKey()));
			for(String t : e.getValue()) {
				log.info(String.format(" - %s", t));
			}
		}
		
	}

	@Override
	public List<OrderedView> getViews(ObjectTemplate template, boolean disableViews) {
		
		if(StringUtils.isNotBlank(template.getTemplateClass())) {
			return getAnnotatedViews(template, disableViews);
		}
		return getDynamicViews(template);
	}

	private List<OrderedView> getDynamicViews(ObjectTemplate template) {
		return null;
	}

	private List<OrderedView> getAnnotatedViews(ObjectTemplate template, boolean disableViews) {
		try {
			Class<?> clz = classService.findClass(template.getTemplateClass());
				
			Map<String, OrderedView> views = new HashMap<>();
			Map<String,String> childViews = new HashMap<>();
			views.put(null, new OrderedView(template.getBundle()));
		
			if(!disableViews) {
				Class<?> tmp = clz;
				
				do {
					ObjectViews annonatedViews = tmp.getAnnotation(ObjectViews.class);
	
					if(Objects.nonNull(annonatedViews)) {
						for(ObjectViewDefinition def : annonatedViews.value()) {
							views.put(def.value(), new OrderedView(def));
						}
					}
					
					tmp = tmp.getSuperclass();
				
				} while(Objects.nonNull(tmp));
			
			}
			
			processFields(null,template, clz, views, new LinkedList<>(), disableViews); 

			for(String child : childViews.keySet()) {
				OrderedView view = views.remove(child);
				views.get(view.getParent()).addChildView(view);
			}
			
			List<OrderedView> results = new ArrayList<>(views.values());
			Collections.sort(results, new Comparator<OrderedView>() {
				@Override
				public int compare(OrderedView o1, OrderedView o2) {
					return o1.getWeight().compareTo(o2.getWeight());
				}
			});
			return results;
		} catch (ClassNotFoundException | NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private void processFields(ObjectView currentView, ObjectTemplate template, Class<?> clz, Map<String, OrderedView> views, LinkedList<FieldTemplate> objectPath, boolean disableViews) throws NoSuchFieldException, ClassNotFoundException {
		
		for(FieldTemplate field : template.getFields()) {
			
			Field f = ReflectionUtils.getField(clz, field.getResourceKey());
			ObjectView v = f.getAnnotation(ObjectView.class);
			
			if(field.getFieldType()==FieldType.OBJECT_EMBEDDED) {
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
			
			if(Objects.isNull(v) || StringUtils.isBlank(v.value())) {
				OrderedView o = views.get(!disableViews && Objects.nonNull(currentView) ? currentView.value() : null);
				if(Objects.isNull(o)) {
					throw new IllegalStateException(
							String.format("No view defined for %s. Did you forget an @ObjectViewDefinition?", v.value()));
				}
				o.addField(new OrderedField(null, o, field, objectPath));
			} else {
				OrderedView o = views.get(disableViews ? null : v.value());
				if(Objects.isNull(o)) {
					throw new IllegalStateException(
							String.format("No view defined for %s. Did you forget an @ObjectViewDefinition?", v.value()));
				}
				o.addField(new OrderedField(v, o, field, objectPath));
			}
		}
	}

	@Override
	public Iterable<ObjectTemplate> allCollectionTemplates() {
		return repository.list(SearchField.eq("type", ObjectType.COLLECTION.name()));
	}

	@Override
	public Class<?> getTemplateClass(String resourceKey) {
		return templateClazzes.get(resourceKey);
	}
	
	@Override
	public String getTemplateResourceKey(Class<?> clz) {
		return templateResourceKeys.get(clz);
	}
}
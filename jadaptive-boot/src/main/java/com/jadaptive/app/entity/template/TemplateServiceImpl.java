package com.jadaptive.app.entity.template;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ObjectTemplateRepository;
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
	
	Map<String,List<ObjectTemplate>> objectForwardDependencies = new HashMap<>();
	Map<String,List<String>> objectReverseDependencies = new HashMap<>();
	
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
	public ObjectTemplate createEntity() {
		return new ObjectTemplate();
	}

	@Override
	public String getName() {
		return "EntityTemplate";
	}
	
	@Override
	public void saveTemplateObjects(List<ObjectTemplate> objects, @SuppressWarnings("unchecked") TransactionAdapter<ObjectTemplate>... ops) throws RepositoryException, ObjectException {
		for(ObjectTemplate obj : objects) {
			saveOrUpdate(validateTemplate(obj));
			switch(obj.getType()) {
			case COLLECTION:
			case SINGLETON:
				permissionService.registerStandardPermissions(obj.getResourceKey());
				break;
			default:
				// Embedded objects do not have direct permissions
			}
			for(TransactionAdapter<ObjectTemplate> op : ops) {
				op.afterSave(obj);
			}
		}
	}

	private ObjectTemplate validateTemplate(ObjectTemplate obj) {
		
		if(!Objects.isNull(obj.getFields())) {
			for(FieldTemplate t : obj.getFields()) {
				switch(t.getFieldType()) {
				case OBJECT_EMBEDDED:
				case OBJECT_REFERENCE:
					if(!validatorPresent(t, ValidationType.OBJECT_TYPE)) {
						throw new ObjectException(String.format("Missing OBJECT_TYPE validator on field %s", t.getResourceKey()));
					}
					break;
				default:
					break;
				}
			}
		}
		
		return obj;
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
	public Iterable<ObjectTemplate> getTemplatesWithScope(ObjectScope personal) {
		return repository.list(SearchField.eq("scope", ObjectScope.PERSONAL.name()));
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
}
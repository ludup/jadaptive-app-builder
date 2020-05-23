package com.jadaptive.app.entity.template;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.entity.ObjectType;
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
import com.jadaptive.app.db.DocumentHelper;

@Service
public class TemplateServiceImpl implements TemplateService, JsonTemplateEnabledService<ObjectTemplate> {

	public static final String RESOURCE_KEY = "entityTemplate";
	
	@Autowired
	private ObjectTemplateRepository repository; 
	
	@Autowired
	private ObjectService entityService;
	
	@Autowired
	private PermissionService permissionService; 
	
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
	public Collection<ObjectTemplate> list() throws RepositoryException, ObjectException {
		
		permissionService.assertRead(RESOURCE_KEY);
		
		return repository.list();
	}
	
	@Override
	public Collection<ObjectTemplate> children(String uuid) {
		
		return repository.list(SearchField.eq("parentTemplate", uuid));
	}
	

	@Override
	public Collection<ObjectTemplate> singletons() throws RepositoryException, ObjectException {
		return repository.list(SearchField.eq("type", ObjectType.SINGLETON.name()));
	}
	
	@Override
	public Collection<ObjectTemplate> table(String searchField, String searchValue, String order, int start, int length) throws RepositoryException, ObjectException {
		
		permissionService.assertRead(RESOURCE_KEY);
		
		return repository.table(searchField, searchValue, order, start, length);
	}
	
	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public void saveOrUpdate(ObjectTemplate template) throws RepositoryException, ObjectException {
		
		permissionService.assertReadWrite(RESOURCE_KEY);
		
		repository.saveOrUpdate(template);
		
	}

	@Override
	public void delete(String uuid) throws ObjectException {
		
		permissionService.assertReadWrite(RESOURCE_KEY);
		
		entityService.deleteAll(uuid);
		repository.delete(uuid);
		
	}
	
	@Override
	public Integer getWeight() {
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
		return RESOURCE_KEY;
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

}
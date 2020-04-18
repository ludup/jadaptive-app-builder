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
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateRepository;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.templates.SystemTemplates;
import com.jadaptive.api.templates.TemplateEnabledService;
import com.jadaptive.app.db.DocumentHelper;
import com.jadaptive.app.entity.MongoEntity;

@Service
public class EntityTemplateServiceImpl implements EntityTemplateService, TemplateEnabledService<EntityTemplate> {

	public static final String RESOURCE_KEY = "entityTemplate";
	
	@Autowired
	private EntityTemplateRepository repository; 
	
	@Autowired
	private EntityService<MongoEntity> entityService;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public EntityTemplate get(String resourceKey) throws RepositoryException, EntityException {
		
		permissionService.assertRead(RESOURCE_KEY);
		
		EntityTemplate e = repository.get(SearchField.or(
				SearchField.eq("resourceKey", resourceKey),
				SearchField.in("aliases", resourceKey)));
		
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Cannot find entity with resource key %s", resourceKey));
		}
		
		return e;
	}

	@Override
	public Collection<EntityTemplate> list() throws RepositoryException, EntityException {
		
		permissionService.assertRead(RESOURCE_KEY);
		
		return repository.list();
	}
	
	@Override
	public Collection<EntityTemplate> table(String searchField, String searchValue, String order, int start, int length) throws RepositoryException, EntityException {
		
		permissionService.assertRead(RESOURCE_KEY);
		
		return repository.table(searchField, searchValue, order, start, length);
	}
	
	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException {
		
		permissionService.assertReadWrite(RESOURCE_KEY);
		
		repository.saveOrUpdate(template);
		
	}

	@Override
	public void delete(String uuid) throws EntityException {
		
		permissionService.assertReadWrite(RESOURCE_KEY);
		
		entityService.deleteAll(uuid);
		repository.delete(uuid);
		
	}
	
	@Override
	public Integer getWeight() {
		return SystemTemplates.ENTITY_TEMPLATE.ordinal();
	}

	@Override
	public Class<EntityTemplate> getResourceClass() {
		return EntityTemplate.class;
	}

	@Override
	public EntityTemplate createEntity() {
		return new EntityTemplate();
	}

	@Override
	public String getName() {
		return "EntityTemplate";
	}
	
	@Override
	public void saveTemplateObjects(List<EntityTemplate> objects, @SuppressWarnings("unchecked") TransactionAdapter<EntityTemplate>... ops) throws RepositoryException, EntityException {
		for(EntityTemplate obj : objects) {
			saveOrUpdate(validateTemplate(obj));
			switch(obj.getType()) {
			case COLLECTION:
			case SINGLETON:
				permissionService.registerStandardPermissions(obj.getResourceKey());
				break;
			default:
				// Embedded objects do not have direct permissions
			}
			for(TransactionAdapter<EntityTemplate> op : ops) {
				op.afterSave(obj);
			}
		}
	}

	private EntityTemplate validateTemplate(EntityTemplate obj) {
		
		if(!Objects.isNull(obj.getFields())) {
			for(FieldTemplate t : obj.getFields()) {
				switch(t.getFieldType()) {
				case OBJECT_EMBEDDED:
				case OBJECT_REFERENCE:
					if(!validatorPresent(t, ValidationType.OBJECT_TYPE)) {
						throw new EntityException(String.format("Missing OBJECT_TYPE validator on field %s", t.getResourceKey()));
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
package com.jadaptive.app.entity.template;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateRepository;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.template.FieldTemplate;
import com.jadaptive.api.template.FieldValidator;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.templates.SystemTemplates;
import com.jadaptive.api.templates.TemplateEnabledService;
import com.jadaptive.app.entity.MongoEntity;

@Service
public class EntityTemplateServiceImpl implements EntityTemplateService, TemplateEnabledService<EntityTemplate> {

	public static final String RESOURCE_KEY = "entityTemplate";
	
	@Autowired
	EntityTemplateRepository repository; 
	
	@Autowired
	EntityService<MongoEntity> entityService;
	
	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	PluginManager pluginManager; 
	
	@Override
	public EntityTemplate get(String resourceKey) throws RepositoryException, EntityException {
		
		permissionService.assertRead(RESOURCE_KEY);
		
		EntityTemplate e = repository.get(SearchField.eq("resourceKey", resourceKey));
		
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
	public void onTemplatesComplete(String... resourceKeys) {
		permissionService.registerStandardPermissions(RESOURCE_KEY);
	}
	
	@Override
	public boolean isSystemOnly() {
		return false;
	}

	@Override
	public String getTemplateFolder() {
		return "templates";
	}

}
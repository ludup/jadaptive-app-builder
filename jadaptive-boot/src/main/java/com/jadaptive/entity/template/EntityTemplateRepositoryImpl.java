package com.jadaptive.entity.template;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.db.AbstractObjectDatabaseImpl;
import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.repository.TransactionAdapter;
import com.jadaptive.templates.SystemTemplates;
import com.jadaptive.templates.TemplateEnabledUUIDRepository;
import com.jadaptive.tenant.TenantService;

@Repository
public class EntityTemplateRepositoryImpl extends AbstractObjectDatabaseImpl 
		implements EntityTemplateRepository, TemplateEnabledUUIDRepository<EntityTemplate> {

	@Autowired
	TenantService tenantService;
	
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
			saveOrUpdate(obj);
			for(TransactionAdapter<EntityTemplate> op : ops) {
				op.afterSave(obj);
			}
		}
	}

	@Override
	public Collection<EntityTemplate> list() throws RepositoryException, EntityException {
		return listObjects(tenantService.getCurrentTenant().getUuid(), EntityTemplate.class);
	}

	@Override
	public EntityTemplate get(String resourceKey) throws RepositoryException, EntityException {
		return getObject(resourceKey, tenantService.getCurrentTenant().getUuid(), EntityTemplate.class);
	}

	@Override
	public void delete(String uuid) throws RepositoryException, EntityException {
		deleteObject(get(uuid), tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException {
		saveObject(template, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public String getResourceKey() {
		return "entityTemplate";
	}

}

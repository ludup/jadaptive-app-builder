package com.jadaptive.entity.template;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.db.AbstractObjectDatabaseImpl;
import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.TenantService;

@Repository
public class EntityTemplateRepositoryImpl extends AbstractObjectDatabaseImpl 
		implements EntityTemplateRepository {

	@Autowired
	TenantService tenantService;

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

}

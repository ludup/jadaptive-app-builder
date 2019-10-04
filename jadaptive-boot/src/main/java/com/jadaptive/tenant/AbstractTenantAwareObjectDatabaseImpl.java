package com.jadaptive.tenant;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.db.AbstractObjectDatabaseImpl;
import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;

public abstract class AbstractTenantAwareObjectDatabaseImpl<T extends AbstractUUIDEntity> 
		extends AbstractObjectDatabaseImpl implements AbstractTenantAwareObjectDatabase<T> {

	
	protected AbstractTenantAwareObjectDatabaseImpl(DocumentDatabase db) {
		super(db);
	}

	@Autowired
	protected TenantService tenantService;
	
	@Override
	public Collection<T> list() throws RepositoryException, EntityException {
		return listObjects(tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}
	
	@Override
	public Collection<T> list(String field, String value) {
		return listObjects(field, value, tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}
	
	@Override
	public Collection<T> matchCollectionObjects(String field, String value) {
		return matchCollectionObjects(field, value, tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}

	@Override
	public T get(String uuid) throws RepositoryException, EntityException {
		return getObject(uuid, tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}
	
	public T get(String field, String value) throws RepositoryException, EntityException {
		return getObject(field, value, tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}

	@Override
	public void delete(T obj) throws RepositoryException, EntityException {
		delete(obj.getUuid());
	}
	
	@Override
	public void delete(String uuid) throws RepositoryException, EntityException {
		deleteObject(get(uuid), tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public void saveOrUpdate(T obj) throws RepositoryException, EntityException {
		saveObject(obj, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public Collection<T> table(String search, String order, int start, int length) {
		return tableObjects(tenantService.getCurrentTenant().getUuid(), getResourceClass(), start, length);
	}

	@Override
	public long count() {
		return countObjects(tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}

}

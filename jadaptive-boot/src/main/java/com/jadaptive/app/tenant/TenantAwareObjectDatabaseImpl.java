package com.jadaptive.app.tenant;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.AbstractObjectDatabaseImpl;
import com.jadaptive.app.db.DocumentDatabase;

@Repository
public class TenantAwareObjectDatabaseImpl<T extends AbstractUUIDEntity> 
		extends AbstractObjectDatabaseImpl implements TenantAwareObjectDatabase<T> {

	protected TenantAwareObjectDatabaseImpl(DocumentDatabase db) {
		super(db);
	}

	
	@Autowired
	protected TenantService tenantService;
	
	@Override
	public Collection<T> list(Class<T> resourceClass, SearchField... fields) {
		return listObjects(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@Override
	public T get(String uuid, Class<T> resourceClass) throws RepositoryException, EntityException {
		return getObject(uuid, tenantService.getCurrentTenant().getUuid(), resourceClass);
	}
	
	public T get(Class<T> resourceClass, SearchField... fields) throws RepositoryException, EntityException {
		return getObject(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(T obj) throws RepositoryException, EntityException {
		delete(obj.getUuid(), (Class<T>) obj.getClass());
	}
	
	@Override
	public void delete(String uuid, Class<T> resourceClass) throws RepositoryException, EntityException {
		deleteObject(get(uuid, resourceClass), tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public void saveOrUpdate(T obj) throws RepositoryException, EntityException {
		saveObject(obj, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public Collection<T> table(String searchField, String searchValue, String order, int start, int length, Class<T> resourceClass) {
		return tableObjects(tenantService.getCurrentTenant().getUuid(), resourceClass, searchField, searchValue, start, length);
	}

	@Override
	public long count(Class<T> resourceClass) {
		return countObjects(tenantService.getCurrentTenant().getUuid(), resourceClass);
	}
	
	@Override
	public Collection<T> searchTable(Class<T> resourceClass, int start, int length, SearchField... fields) {
		return searchTable(tenantService.getCurrentTenant().getUuid(), resourceClass, start, length, fields);
	}
	
	@Override
	public Collection<T> searchObjects(Class<T> resourceClass, SearchField... fields) {
		return searchObjects(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@Override
	public Long searchCount(Class<T> resourceClass, SearchField... fields) {
		return searchCount(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}
}

package com.jadaptive.app.tenant;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.AbstractTenantAwareObjectDatabase;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.AbstractObjectDatabaseImpl;
import com.jadaptive.app.db.DocumentDatabase;

public abstract class AbstractTenantAwareObjectDatabaseImpl<T extends AbstractUUIDEntity> 
		extends AbstractObjectDatabaseImpl implements AbstractTenantAwareObjectDatabase<T> {

	
	protected AbstractTenantAwareObjectDatabaseImpl(DocumentDatabase db) {
		super(db);
	}

	@Autowired
	protected TenantService tenantService;
	
	@Override
	public Iterable<T> list() throws RepositoryException, ObjectException {
		return listObjects(tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}
	
	@Override
	public Iterable<T> list(SearchField... fields) {
		return listObjects(tenantService.getCurrentTenant().getUuid(), getResourceClass(), fields);
	}
	
	@Override
	public Collection<T> searchObjects(SearchField... fields) {
		return searchObjects(tenantService.getCurrentTenant().getUuid(), getResourceClass(), fields);
	}
	
	@Override
	public Collection<T> searchTable(int start, int length, SearchField... fields) {
		return searchTable(tenantService.getCurrentTenant().getUuid(), getResourceClass(), start, length, fields);
	}
	
	@Override
	public Long searchCount(SearchField... fields) {
		return searchCount(tenantService.getCurrentTenant().getUuid(), getResourceClass(), fields);
	}

	@Override
	public T get(String uuid) throws RepositoryException, ObjectException {
		return getObject(uuid, tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}
	
	public T get(ObjectTemplate template, SearchField... fields) throws RepositoryException, ObjectException {
		return getObject(tenantService.getCurrentTenant().getUuid(), getResourceClass(), fields);
	}

	@Override
	public void delete(T obj) throws RepositoryException, ObjectException {
		delete(obj.getUuid());
	}
	
	@Override
	public void delete(String uuid) throws RepositoryException, ObjectException {
		deleteObject(get(uuid), tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public void saveOrUpdate(T obj) throws RepositoryException, ObjectException {
		saveObject(obj, tenantService.getCurrentTenant().getUuid());
	}
	
	@Override
	public void saveOrUpdate(T obj, ObjectTemplate template) throws RepositoryException, ObjectException {
		saveObject(obj, template, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public Collection<T> table(String searchField, String searchValue, String order, int start, int length) {
		return tableObjects(tenantService.getCurrentTenant().getUuid(), getResourceClass(), searchField, searchValue, start, length);
	}

	@Override
	public long count() {
		return countObjects(tenantService.getCurrentTenant().getUuid(), getResourceClass());
	}

}

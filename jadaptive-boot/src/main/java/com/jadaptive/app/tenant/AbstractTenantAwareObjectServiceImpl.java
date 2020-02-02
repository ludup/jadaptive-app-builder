package com.jadaptive.app.tenant;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.tenant.AbstractTenantAwareObjectService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;

public abstract class AbstractTenantAwareObjectServiceImpl<T extends AbstractUUIDEntity> 
					implements AbstractTenantAwareObjectService<T>, TenantAware {

	
	@Autowired
	PermissionService permissionService; 
	
	@PostConstruct
	private void postConstruct() {
	
	}
	
	@Override
	public void initializeSystem() {
		permissionService.registerStandardPermissions(getResourceKey());
	}
	
	@Override
	public void initializeTenant(Tenant tenant) {

	}
	
	protected String getResourceKey() {
		return getRepository().getResourceClass().getSimpleName().substring(0,1).toLowerCase()
				+ getRepository().getResourceClass().getSimpleName().substring(1);
	}
	
	protected void assertReadWrite() {
		permissionService.assertReadWrite(getResourceKey());
	}
	
	protected void assertRead() {
		permissionService.assertRead(getResourceKey());
	}
	
	@Override
	public T get(String uuid) throws RepositoryException, EntityException {
		
		assertRead();
		
		T e = getRepository().get(uuid);
		
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Cannot find %s with id %s", getResourceKey(), uuid));
		}
		
		return e;
	}
	
	@Override
	public T get(SearchField... fields) throws RepositoryException, EntityException {
		
		assertRead();
		
		T e = getRepository().get(fields);
		
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Cannot find %s", getResourceKey()));
		}
		
		return e;
	}

	@Override
	public Collection<T> list() throws RepositoryException, EntityException {
		assertRead();
		return getRepository().list();
	}
	
	@Override
	public Collection<T> table(String searchField, String searchValue,  String order, int start, int length) throws RepositoryException, EntityException {
		assertRead();
		return getRepository().table(searchField, searchValue, order, start, length);
	}
	
	@Override
	public long count() {
		assertRead();
		return getRepository().count();
	}

	@Override
	public void saveOrUpdate(T obj) throws RepositoryException, EntityException {
		assertReadWrite();
		getRepository().saveOrUpdate(obj);
		
	}

	@Override
	public void delete(T obj) throws EntityException {
		
		delete(obj.getUuid());
	}
	
	@Override
	public void delete(String uuid) throws EntityException {
		
		assertReadWrite();
		beforeDelete(uuid);
		getRepository().delete(uuid);
		afterDelete(uuid);
	}

	protected void afterDelete(String uuid) {

	}

	protected void beforeDelete(String uuid) {
		
	}
}

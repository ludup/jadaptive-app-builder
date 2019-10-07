package com.jadaptive.tenant;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.entity.EntityException;
import com.jadaptive.permissions.PermissionService;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;

public abstract class AbstractTenantAwareObjectServiceImpl<T extends AbstractUUIDEntity> implements AbstractTenantAwareObjectService<T> {

	
	@Autowired
	PermissionService permissionService; 
	
	@PostConstruct
	private void postConstruct() {
		permissionService.registerStandardPermissions(getResourceKey());
		
	}
	
	protected String getResourceKey() {
		return getRepository().getResourceClass().getSimpleName().substring(0,1).toLowerCase()
				+ getRepository().getResourceClass().getSimpleName().substring(1);
	}
	
	protected void assertReadWrite() {
		permissionService.assertReadWrite(getResourceKey());
	}
	
	@Override
	public T get(String uuid) throws RepositoryException, EntityException {
		
		
		T e = getRepository().get(uuid);
		
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Cannot find %s with id %s", getResourceKey(), uuid));
		}
		
		return e;
	}
	
	@Override
	public T get(String field, String value) throws RepositoryException, EntityException {
		
		
		T e = getRepository().get(field, value);
		
		if(Objects.isNull(e)) {
			throw new EntityException(String.format("Cannot find %s with %s %s", getResourceKey(), field, value));
		}
		
		return e;
	}

	@Override
	public Collection<T> list() throws RepositoryException, EntityException {
		
		return getRepository().list();
	}
	
	@Override
	public Collection<T> table(String search, String order, int start, int length) throws RepositoryException, EntityException {
		
		return getRepository().table(search, order, start, length);
	}
	
	@Override
	public long count() {
		return getRepository().count();
	}

	@Override
	public void saveOrUpdate(T obj) throws RepositoryException, EntityException {
		
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

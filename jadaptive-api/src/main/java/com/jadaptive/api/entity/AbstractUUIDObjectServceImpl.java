package com.jadaptive.api.entity;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.UUIDEntity;

public abstract class AbstractUUIDObjectServceImpl<T extends UUIDEntity> extends AuthenticatedService implements AbstractUUIDObjectService<T> {

	@Autowired
	protected TenantAwareObjectDatabase<T> objectDatabase;
	
	protected abstract Class<T> getResourceClass();
	@Override
	public T getObjectByUUID(String uuid) {
		return objectDatabase.get(uuid, getResourceClass());
	}

	@Override
	public String saveOrUpdate(T object) {
		validateSave(object);
		objectDatabase.saveOrUpdate(object);
		return object.getUuid();
	}

	@Override
	public void deleteObject(T object) {
		validateDelete(object);
		objectDatabase.delete(object);
	}

	protected void validateDelete(T object) {
		
	}
	
	@Override
	public void deleteObjectByUUID(String uuid) {
		T object = getObjectByUUID(uuid);
		validateDelete(object);
		objectDatabase.delete(object);
	}

	@Override
	public Iterable<T> allObjects() {
		return objectDatabase.list(getResourceClass());
	}
	
	protected void validateSave(T object) {
		
	}

}

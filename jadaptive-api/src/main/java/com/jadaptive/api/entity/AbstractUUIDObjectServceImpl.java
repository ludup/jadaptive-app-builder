package com.jadaptive.api.entity;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.repository.UUIDEntity;

public abstract class AbstractUUIDObjectServceImpl<T extends UUIDEntity> implements AbstractUUIDObjectService<T> {

	@Autowired
	protected TenantAwareObjectDatabase<T> objectDatabase;
	
	protected abstract Class<T> getResourceClass();
	@Override
	public T getObjectByUUID(String uuid) {
		return objectDatabase.get(uuid, getResourceClass());
	}

	@Override
	public String saveOrUpdate(T object) {
		objectDatabase.saveOrUpdate(object);
		return object.getUuid();
	}

	@Override
	public void deleteObject(T object) {
		objectDatabase.delete(object);
	}

	@Override
	public void deleteObjectByUUID(String uuid) {
		objectDatabase.delete(getObjectByUUID(uuid));
	}

	@Override
	public Iterable<T> allObjects() {
		return objectDatabase.list(getResourceClass());
	}

}

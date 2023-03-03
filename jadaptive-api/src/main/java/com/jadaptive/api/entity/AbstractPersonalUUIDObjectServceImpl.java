package com.jadaptive.api.entity;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.PersonalUUIDEntity;

public abstract class AbstractPersonalUUIDObjectServceImpl<T extends PersonalUUIDEntity> extends AuthenticatedService implements AbstractUUIDObjectService<T> {

	@Autowired
	protected PersonalObjectDatabase<T> objectDatabase;
	
	protected abstract Class<T> getResourceClass();
	@Override
	public T getObjectByUUID(String uuid) {
		return objectDatabase.getObjectByUUID(getResourceClass(), uuid);
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
		objectDatabase.deletePersonalObject(object);
	}

	protected void validateDelete(T object) {
		
	}
	
	@Override
	public void deleteObjectByUUID(String uuid) {
		T object = getObjectByUUID(uuid);
		validateDelete(object);
		objectDatabase.deletePersonalObject(object);
	}

	@Override
	public Iterable<T> allObjects() {
		return objectDatabase.allObjects(getResourceClass());
	}
	
	protected void validateSave(T object) {
		
	}

}

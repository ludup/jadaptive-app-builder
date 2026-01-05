package com.jadaptive.api.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;

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
		objectDatabase.saveOrUpdate(object, getCurrentUser());
		return object.getUuid();
	}
	
	@Override
	public void createIfNotExisting(T obj) {
		if(StringUtils.isBlank(obj.getUuid())) {
			throw new IllegalArgumentException("Expected UUIDDocument with initialised UUID value!");
		}
		if(Objects.isNull(obj.getOwner())) {
			obj.setOwner(getCurrentUser());
		}
		try {
			objectDatabase.getObjectByUUID(getResourceClass(),obj.getUuid());
		} catch(ObjectNotFoundException e) { 
			saveOrUpdate(obj);
		}
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
	public Iterable<T> allObjects(SearchField... fields) {
		return objectDatabase.allObjects(getResourceClass(), fields);
	}
	
	protected void validateSave(T object) {
		
	}

	@Override
	public void deleteAll() {
		streamAll().forEach(objectDatabase::deletePersonalObject);
	}

	@Override
	public Collection<? extends UUIDDocument> searchTable(int start, int length, SortOrder order, String sortField,
			SearchField... fields) {
		return objectDatabase.searchPersonalObjects(getResourceClass(), sortField, sortField, start, length);
	}

	@Override
	public long countTable(SearchField... fields) {
		return objectDatabase.getPersonalObjectCount(getResourceClass(), getCurrentUser(), fields);
	}

	@Override
	@Deprecated(since = "0.6.0", forRemoval = true)
	public Collection<T> collection(SearchField... fields) {
		return objectDatabase.getPersonalObjects(getResourceClass(), getCurrentUser(), fields);
	}
	
	 public T createNew(ObjectTemplate template) { try {
		return getResourceClass().getConstructor().newInstance();
	} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
			| NoSuchMethodException | SecurityException e) {
		throw new IllegalStateException(e.getMessage(), e);
	} }

}

package com.jadaptive.api.entity;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.AssignableDocument;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.SortOrder;

public abstract class AbstractAssignableUUIDObjectServiceImpl <T extends AssignableDocument> extends AuthenticatedService implements AbstractUUIDObjectService<T> {

	@Autowired
	protected AssignableObjectDatabase<T> objectDatabase;
	
	protected abstract Class<T> getResourceClass();
	
	@Override
	public T getObjectByUUID(String uuid) {
		return objectDatabase.getObjectByUUID(getResourceClass(), uuid);
	}

	@Override
	public String saveOrUpdate(T object) {
		beforeSave(object);
		objectDatabase.saveOrUpdate(object);
		afterSave(object);
		return object.getUuid();
	}

	@Override
	public void createIfNotExisting(T obj) {
		if(StringUtils.isBlank(obj.getUuid())) {
			throw new IllegalArgumentException("Expected UUIDDocument with initialised UUID value!");
		}
		try {
			objectDatabase.getObjectByUUID(getResourceClass(),obj.getUuid());
		} catch(ObjectNotFoundException e) { 
			saveOrUpdate(obj);
		}
	}
	
	protected void beforeSave(T object) {
		
	}
	
	protected void afterSave(T object) {
		
	}

	@Override
	public void deleteObject(T object) {
		objectDatabase.deleteObject(object);
	}

	@Override
	public void deleteObjectByUUID(String uuid) {
		objectDatabase.deleteObject(getObjectByUUID(uuid));
	}

	@Override
	public Iterable<T> allObjects() {
		return objectDatabase.searchObjects(getResourceClass());
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<? extends UUIDDocument> searchTable(int start, int length, SortOrder order, String sortField,
			SearchField... fields) {
		return objectDatabase.searchObjects(getResourceClass(), fields);
	}

	@Override
	public long countTable(SearchField... fields) {
		return objectDatabase.countObjects(getResourceClass(), fields);
	}

	@Override
	public Collection<T> collection(SearchField... fields) {
		return objectDatabase.searchObjects(getResourceClass(), fields);
	}
}

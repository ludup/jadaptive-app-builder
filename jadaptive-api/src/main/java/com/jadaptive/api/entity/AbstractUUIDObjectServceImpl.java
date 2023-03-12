package com.jadaptive.api.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.SortOrder;

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
		beforeSave(object);
		objectDatabase.saveOrUpdate(object);
		afterSave(object);
		return object.getUuid();
	}
	
	

	@Override
	public UUIDDocument createNew() {
		
		try {
			T obj = getResourceClass().getDeclaredConstructor().newInstance();
			setupDefaults(obj);
			return obj;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ObjectException(e.getMessage(), e);
		}
		
	}
	
	protected void setupDefaults(T object) {
		
	}
	@Override
	public void deleteObject(T object) {
		validateDelete(object);
		objectDatabase.delete(object);
	}

	protected void validateDelete(T object) {
		
	}
	
	protected void afterSave(T object) {
		
	}
	
	@Override
	public void deleteObjectByUUID(String uuid) {
		T object = getObjectByUUID(uuid);
		validateDelete(object);
		objectDatabase.delete(object);
	}
	
	@Override
	public void deleteAll() {
		objectDatabase.deleteAll();
	}

	@Override
	public Iterable<T> allObjects() {
		return objectDatabase.list(getResourceClass());
	}
	
	protected void beforeSave(T object) {
		
	}
	
	@Override
	public Collection<? extends UUIDDocument> searchTable(int start, int length, SortOrder sort, String sortField, SearchField... fields) {
		return objectDatabase.searchTable(getResourceClass(), start, length, sort, sortField, fields);
	}
	
	@Override
	public long countTable(SearchField... fields) {
		return objectDatabase.count(getResourceClass(), fields);
	}

}

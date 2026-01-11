package com.jadaptive.app.tenant;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.user.User;

@Repository
public class PersonalObjectDatabaseImpl<T extends PersonalUUIDEntity> 
		 implements PersonalObjectDatabase<T> {

	@Autowired
	TenantAwareObjectDatabase<T> objectDatabase;
	
	@Override
	public Collection<T> getPersonalObjects(Class<T> resourceClass, User user) {
		
		return objectDatabase.searchObjects(resourceClass, 
				SearchField.or(
						SearchField.eq("ownerUUID", user.getUuid()),
						SearchField.eq("owner.uuid", user.getUuid())));
	}
	
	@Override
	public Iterable<T> allPersonalObjects(Class<T> resourceClass, User user, SearchField... search) {
		if(search.length > 0) {
			return objectDatabase.list(resourceClass,
				Stream.concat(Stream.of(search), Stream.of(SearchField.or(
						SearchField.eq("ownerUUID", user.getUuid()),
						SearchField.eq("owner.uuid", user.getUuid())))).toList().toArray(new SearchField[0])
				);
		}
		else {
			return objectDatabase.list(resourceClass, 
					SearchField.or(
							SearchField.eq("ownerUUID", user.getUuid()),
							SearchField.eq("owner.uuid", user.getUuid())));
		}
	}

	@Override
	public Collection<T> getPersonalObjects(Class<T> resourceClass, User user, SearchField... search) {
		if(search.length > 0) {
			return objectDatabase.searchObjects(resourceClass, 
					SearchField.or(
							SearchField.eq("ownerUUID", user.getUuid()),
							SearchField.eq("owner.uuid", user.getUuid())), SearchField.and(search));
		} else {
			return objectDatabase.searchObjects(resourceClass, 
					SearchField.or(
							SearchField.eq("ownerUUID", user.getUuid()),
							SearchField.eq("owner.uuid", user.getUuid())));
		}
	}
	
	@Override
	public T getPersonalObject(Class<T> resourceClass, User user, String uuid) {
		return objectDatabase.get(resourceClass, 
					SearchField.eq("uuid", uuid),
					SearchField.or(
							SearchField.eq("ownerUUID", user.getUuid()),
							SearchField.eq("owner.uuid", user.getUuid())));
	}
	
	@Override
	public Long getPersonalObjectCount(Class<T> resourceClass, User user, SearchField... search) {
		if(search.length > 0) {
			return objectDatabase.searchCount(resourceClass, 
					SearchField.or(
							SearchField.eq("ownerUUID", user.getUuid()),
							SearchField.eq("owner.uuid", user.getUuid())), SearchField.and(search));
		} else {
			return objectDatabase.searchCount(resourceClass, 
					SearchField.or(
							SearchField.eq("ownerUUID", user.getUuid()),
							SearchField.eq("owner.uuid", user.getUuid())));
		}
	}
	
	@Override
	public T getPersonalObject(Class<T> resourceClass, User user, SearchField... search) {
		if(search.length > 0) {
			return objectDatabase.get(resourceClass, 
					SearchField.or(
							SearchField.eq("ownerUUID", user.getUuid()),
							SearchField.eq("owner.uuid", user.getUuid())), SearchField.and(search));
		} else {
			return objectDatabase.get(resourceClass, 
					SearchField.or(
							SearchField.eq("ownerUUID", user.getUuid()),
							SearchField.eq("owner.uuid", user.getUuid())));
		}
	}
	
	@Override
	public void saveOrUpdate(T obj, User user) {
		obj.setOwner(user);
		objectDatabase.saveOrUpdate(obj);
	}
	
	@Override
	public void saveOrUpdate(T obj) {
		if(Objects.isNull(obj.getOwner())) {
			throw new ObjectException("Personal object cannot be saved without an owner UUID");
		}
		objectDatabase.saveOrUpdate(obj);
	}

	@Override
	public Collection<T> searchPersonalObjects(Class<T> resourceClass, String searchColumn, String searchPattern, int start,
			int length) {
		return objectDatabase.searchObjects(resourceClass, SearchField.like(searchColumn, searchPattern));
	}

	@Override
	public Long searchPersonalObjectsCount(Class<T> resourceClass, String searchColumn, String searchPattern) {
		return objectDatabase.searchCount(resourceClass, SearchField.like(searchColumn, searchPattern));
	}

	@Override
	public void deletePersonalObject(T obj) {
		objectDatabase.delete(obj);
	}

	@Override
	public Iterable<T> allObjects(Class<T> resourceClass) {
		return objectDatabase.list(resourceClass);
	}

	@Override
	public Iterable<T> allObjects(Class<T> resourceClass, SearchField... searchFields) {
		return objectDatabase.list(resourceClass, searchFields);
	}

	@Override
	public T getObjectByUUID(Class<T> resourceClass, String uuid) {
		return objectDatabase.get(uuid, resourceClass);
	}

	@Override
	public long allObjectsCount(Class<T> resourceClass, SearchField...fields) {
		return objectDatabase.count(resourceClass, fields);
	}

	@Override
	public T max(Class<T> resourceClass, String field, SearchField... fields) {
		return objectDatabase.max(resourceClass, field, fields);
	}

	@Override
	public T getObject(Class<T> resourceClass, SearchField... search) {
		return objectDatabase.get(resourceClass, search);
	}
}

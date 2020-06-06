package com.jadaptive.api.db;

import java.util.Collection;

import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.user.User;

public interface PersonalObjectDatabase<T extends PersonalUUIDEntity> {

	Collection<T> getPersonalObjects(Class<T> resourceClass, User user);

	void saveOrUpdate(T obj, User user);

	Collection<T> getPersonalObjects(Class<T> resourceClass, User user, SearchField... search);

	T getPersonalObject(Class<T> resourceClass, User user, SearchField... search);

	Collection<T> searchPersonalObjects(Class<T> resourceClass,
			String searchColumn, String searchPattern, int start, int length);

	Long searchPersonalObjectsCount(Class<T> resourceClass, String searchColumn, String searchPattern);

	void deletePersonalObject(T obj);

	Iterable<T> allObjects(Class<T> resourceClass);
	
	Iterable<T> allObjects(Class<T> resourceClass, SearchField... searchFields);

	void saveOrUpdate(T obj);
}

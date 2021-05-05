package com.jadaptive.api.db;

import java.util.Collection;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;

public interface TenantAwareObjectDatabase<T extends UUIDEntity> {

	Iterable<T> list(Class<T> resourceClass, SearchField...fields);

	T get(String uuid, Class<T> resourceClass) throws RepositoryException, ObjectException;
	
	T get(Class<T> resourceClass, SearchField... fields) throws RepositoryException, ObjectException;

	void delete(T obj) throws RepositoryException, ObjectException;

	void delete(String uuid, Class<T> resourceClass) throws RepositoryException, ObjectException;
	
	void saveOrUpdate(T obj) throws RepositoryException, ObjectException;

	Collection<T> table(String searchField, String searchValue, String order, int start, int length,
			Class<T> resourceClass);

	long count(Class<T> resourceClass, SearchField... fields);

	Collection<T> searchTable(Class<T> resourceClass, int start, int length, SearchField... fields);

	Collection<T> searchObjects(Class<T> resourceClass, SearchField... fields);

	Long searchCount(Class<T> resourceClass, SearchField... fields);

	T max(Class<T> resourceClass, String field) throws RepositoryException, ObjectException;

	T min(Class<T> resourceClass, String field) throws RepositoryException, ObjectException;

}

package com.jadaptive.api.db;

import java.util.Collection;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;

public interface TenantAwareObjectDatabase<T extends AbstractUUIDEntity> {

	Collection<T> list(Class<T> resourceClass, SearchField...fields);

	T get(String uuid, Class<T> resourceClass) throws RepositoryException, EntityException;
	
	T get(Class<T> resourceClass, SearchField... fields) throws RepositoryException, EntityException;

	void delete(T obj) throws RepositoryException, EntityException;

	void delete(String uuid, Class<T> resourceClass) throws RepositoryException, EntityException;
	
	void saveOrUpdate(T obj) throws RepositoryException, EntityException;

	Collection<T> table(String searchField, String searchValue, String order, int start, int length,
			Class<T> resourceClass);

	long count(Class<T> resourceClass);

	Collection<T> searchTable(Class<T> resourceClass, int start, int length, SearchField... fields);

	Collection<T> searchObjects(Class<T> resourceClass, SearchField... fields);

	Long searchCount(Class<T> resourceClass, SearchField... fields);

}

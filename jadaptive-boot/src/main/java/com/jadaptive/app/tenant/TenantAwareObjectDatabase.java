package com.jadaptive.app.tenant;

import java.util.Collection;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;

public interface TenantAwareObjectDatabase<T extends AbstractUUIDEntity> {

	Collection<T> list(Class<T> resourceClass) throws RepositoryException, EntityException;

	Collection<T> list(String field, String value, Class<T> resourceClass);

	Collection<T> matchCollectionObjects(String field, String value, Class<T> resourceClass);

	T get(String uuid, Class<T> resourceClass) throws RepositoryException, EntityException;

	void delete(T obj) throws RepositoryException, EntityException;

	void delete(String uuid, Class<T> resourceClass) throws RepositoryException, EntityException;
	
	void saveOrUpdate(T obj) throws RepositoryException, EntityException;

	Collection<T> table(String searchField, String searchValue, String order, int start, int length,
			Class<T> resourceClass);



	long count(Class<T> resourceClass);

}

package com.jadaptive.api.entity;

import java.util.Collection;

import com.jadaptive.api.repository.RepositoryException;

public interface EntityService<E extends AbstractEntity> {

	E get(String resourceKey, String uuid) throws RepositoryException, EntityException;

	E getSingleton(String resourceKey) throws RepositoryException, EntityException;

	Collection<E> list(String resourceKey) throws RepositoryException, EntityException;

	void saveOrUpdate(E entity) throws RepositoryException, EntityException;

	void delete(String resourceKey, String uuid) throws RepositoryException, EntityException;

	void deleteAll(String resourceKey) throws EntityException;

	Collection<E> table(String resourceKey, String searchField, String searchValue, int offset, int limit);

	long count(String resourceKey);

	long count(String resourceKey, String searchField, String searchValue);
}

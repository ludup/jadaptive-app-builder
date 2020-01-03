package com.jadaptive.api.entity;

import java.util.Collection;

import com.jadaptive.app.repository.RepositoryException;

public interface EntityRepository<E extends AbstractEntity> {

	Collection<E> list(String resourceKey) throws RepositoryException, EntityException;

	E get(String uuid, String resourceKey) throws RepositoryException, EntityException;

	void delete(String resourceKey, String uuid) throws RepositoryException, EntityException;

	void deleteAll(String resourceKey) throws RepositoryException, EntityException;

	void save(E entity) throws RepositoryException, EntityException;

	Collection<E> table(String resourceKey, String searchField, String searchValue, int offset, int limit);

	long count(String resourceKey);

	long count(String resourceKey, String searchField, String searchValue);


}

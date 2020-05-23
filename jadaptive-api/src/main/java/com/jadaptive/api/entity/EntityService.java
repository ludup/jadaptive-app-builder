package com.jadaptive.api.entity;

import java.util.Collection;

import com.jadaptive.api.repository.RepositoryException;

public interface EntityService {

	AbstractEntity get(String resourceKey, String uuid) throws RepositoryException, EntityException;

	AbstractEntity getSingleton(String resourceKey) throws RepositoryException, EntityException;

	Collection<AbstractEntity> list(String resourceKey) throws RepositoryException, EntityException;

	String saveOrUpdate(AbstractEntity entity) throws RepositoryException, EntityException;

	void delete(String resourceKey, String uuid) throws RepositoryException, EntityException;

	void deleteAll(String resourceKey) throws EntityException;

	Collection<AbstractEntity> table(String resourceKey, String searchField, String searchValue, int offset, int limit);

	long count(String resourceKey);

	long count(String resourceKey, String searchField, String searchValue);
}

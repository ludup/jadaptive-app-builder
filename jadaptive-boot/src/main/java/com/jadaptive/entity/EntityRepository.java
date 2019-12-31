package com.jadaptive.entity;

import java.util.Collection;

import com.jadaptive.repository.RepositoryException;

public interface EntityRepository {

	Collection<Entity> list(String resourceKey) throws RepositoryException, EntityException;

	Entity get(String uuid, String resourceKey) throws RepositoryException, EntityException;

	void delete(String resourceKey, String uuid) throws RepositoryException, EntityException;

	void deleteAll(String resourceKey) throws RepositoryException, EntityException;

	void save(Entity entity) throws RepositoryException, EntityException;

	Collection<Entity> table(String resourceKey, String searchField, String searchValue, int offset, int limit);

	long count(String resourceKey);

	long count(String resourceKey, String searchField, String searchValue);


}

package com.jadaptive.entity;

import java.util.Collection;

import com.jadaptive.repository.RepositoryException;

public interface EntityService {

	Entity get(String resourceKey, String uuid) throws RepositoryException, EntityException;

	Entity getSingleton(String resourceKey) throws RepositoryException, EntityException;

	Collection<Entity> list(String resourceKey) throws RepositoryException, EntityException;

	void saveOrUpdate(Entity entity) throws RepositoryException, EntityException;

	void delete(String resourceKey, String uuid) throws RepositoryException, EntityException;

	void deleteAll(String resourceKey) throws EntityException;
}

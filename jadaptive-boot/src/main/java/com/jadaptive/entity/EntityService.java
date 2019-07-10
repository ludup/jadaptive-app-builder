package com.jadaptive.entity;

import java.util.Collection;

import com.jadaptive.repository.RepositoryException;

public interface EntityService {

	Entity get(String resourceKey, String uuid) throws RepositoryException, EntityNotFoundException;

	Entity getSingleton(String resourceKey) throws RepositoryException, EntityNotFoundException;

	Collection<Entity> list(String resourceKey) throws RepositoryException, EntityNotFoundException;

	void saveOrUpdate(String resourceKey, Entity entity) throws RepositoryException, EntityNotFoundException;

	void delete(String resourceKey, String uuid) throws RepositoryException, EntityNotFoundException;
}

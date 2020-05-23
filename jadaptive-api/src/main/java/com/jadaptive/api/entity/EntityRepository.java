package com.jadaptive.api.entity;

import java.util.Collection;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.repository.RepositoryException;

public interface EntityRepository {

	Collection<AbstractEntity> list(String resourceKey, SearchField... fields) throws RepositoryException, EntityException;

	AbstractEntity get(String uuid, String resourceKey) throws RepositoryException, EntityException;

	void delete(String resourceKey, String uuid) throws RepositoryException, EntityException;

	void deleteAll(String resourceKey) throws RepositoryException, EntityException;

	String save(AbstractEntity entity) throws RepositoryException, EntityException;

	Collection<AbstractEntity> table(String resourceKey, String searchField, String searchValue, int offset, int limit);

	long count(String resourceKey);

	long count(String resourceKey, String searchField, String searchValue);


}

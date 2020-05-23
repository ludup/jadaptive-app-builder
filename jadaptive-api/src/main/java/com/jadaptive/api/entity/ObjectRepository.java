package com.jadaptive.api.entity;

import java.util.Collection;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.repository.RepositoryException;

public interface ObjectRepository {

	Collection<AbstractObject> list(String resourceKey, SearchField... fields) throws RepositoryException, ObjectException;

	AbstractObject get(String uuid, String resourceKey) throws RepositoryException, ObjectException;

	void delete(String resourceKey, String uuid) throws RepositoryException, ObjectException;

	void deleteAll(String resourceKey) throws RepositoryException, ObjectException;

	String save(AbstractObject entity) throws RepositoryException, ObjectException;

	Collection<AbstractObject> table(String resourceKey, String searchField, String searchValue, int offset, int limit);

	long count(String resourceKey);

	long count(String resourceKey, String searchField, String searchValue);


}

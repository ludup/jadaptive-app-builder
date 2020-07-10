package com.jadaptive.api.entity;

import java.util.Collection;

import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.ObjectTemplate;

public interface ObjectService {

	AbstractObject createNew(ObjectTemplate template);
	
	AbstractObject get(String resourceKey, String uuid) throws RepositoryException, ObjectException;

	AbstractObject getSingleton(String resourceKey) throws RepositoryException, ObjectException;

	Iterable<AbstractObject> list(String resourceKey) throws RepositoryException, ObjectException;

	String saveOrUpdate(AbstractObject entity) throws RepositoryException, ObjectException;

	void delete(String resourceKey, String uuid) throws RepositoryException, ObjectException;

	void deleteAll(String resourceKey) throws ObjectException;

	Collection<AbstractObject> table(String resourceKey, String searchField, String searchValue, int offset, int limit);

	long count(String resourceKey);

	long count(String resourceKey, String searchField, String searchValue);

	Collection<AbstractObject> personal(String resourceKey) throws RepositoryException, ObjectException;
}

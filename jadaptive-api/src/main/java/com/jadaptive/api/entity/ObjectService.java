package com.jadaptive.api.entity;

import java.io.IOException;
import java.util.Collection;

import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationException;

public interface ObjectService {

	AbstractObject createNew(ObjectTemplate template);
	
	AbstractObject get(String resourceKey, String uuid) throws RepositoryException, ObjectException, ValidationException;

	AbstractObject getSingleton(String resourceKey) throws RepositoryException, ObjectException, ValidationException;

	Iterable<AbstractObject> list(String resourceKey) throws RepositoryException, ObjectException;

	String saveOrUpdate(AbstractObject entity) throws RepositoryException, ObjectException;

	void delete(String resourceKey, String uuid) throws RepositoryException, ObjectException;

	void deleteAll(String resourceKey) throws ObjectException;

	Collection<AbstractObject> table(String resourceKey, String searchField, String searchValue, int offset, int limit);

	long count(String resourceKey, String searchField, String searchValue);

	FormHandler getFormHandler(String handler);

	AbstractObject get(ObjectTemplate template, String uuid)
			throws RepositoryException, ObjectException, ValidationException;

	void assertForiegnReferences(ObjectTemplate template, String uuid);

	void rebuildReferences(ObjectTemplate template);

	AbstractObject convert(UUIDDocument obj);

	<T extends UUIDDocument> void stashObject(AbstractObject obj) throws ValidationException, RepositoryException, ObjectException, IOException;

	<T extends UUIDDocument> void stashObject(T obj) throws ValidationException, RepositoryException, ObjectException, IOException;

	<T extends UUIDDocument> T fromStash(String resourceKey, Class<T> clz);

	
}

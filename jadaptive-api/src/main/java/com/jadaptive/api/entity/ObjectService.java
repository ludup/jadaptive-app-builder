package com.jadaptive.api.entity;

import java.io.IOException;
import java.util.Collection;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDReference;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.template.ValidationException;

public interface ObjectService {

	AbstractObject createNew(ObjectTemplate template);
	
	AbstractObject get(String resourceKey, String uuid) throws RepositoryException, ObjectException, ValidationException;

	AbstractObject getSingleton(String resourceKey) throws RepositoryException, ObjectException, ValidationException;

	Iterable<AbstractObject> list(String resourceKey) throws RepositoryException, ObjectException;

	String saveOrUpdate(AbstractObject entity) throws RepositoryException, ObjectException;

	void delete(String resourceKey, String uuid) throws RepositoryException, ObjectException;

	long count(String resourceKey, String searchField, String searchValue);

	FormHandler getFormHandler(String handler);

	AbstractObject get(ObjectTemplate template, String uuid)
			throws RepositoryException, ObjectException, ValidationException;

	void assertForiegnReferences(ObjectTemplate template, String uuid);

	void rebuildReferences(ObjectTemplate template);

	AbstractObject toAbstractObject(UUIDDocument obj);

	<T extends UUIDDocument> void stashObject(AbstractObject obj) throws ValidationException, RepositoryException, ObjectException, IOException;

	<T extends UUIDDocument> void stashObject(T obj) throws ValidationException, RepositoryException, ObjectException, IOException;

	<T extends UUIDDocument> T fromStash(String resourceKey, Class<T> clz);
	
	<T extends UUIDDocument> T peekStash(String resourceKey, Class<T> clz);

	Collection<AbstractObject> convertObjects(Iterable<? extends UUIDDocument> objects);

	AbstractObject fromStashToAbstractObject(String resourceKey);

	long countObjects(String resourceKey, SearchField... fields);

	Collection<AbstractObject> tableObjects(String resourceKey, int offset,
			int limit, String sortColumn, SortOrder order, SearchField... fields);

	UUIDDocument toUUIDDocument(AbstractObject entity);

	Collection<AbstractObject> table(String resourceKey, String searchField, String searchValue, int offset, int limit,
			String sortColumn, SortOrder order);

	void cascadeDelete(ObjectTemplate template, AbstractObject e);

	AbstractObject createNew(String resourceKey);

	void deleteAll(String resourceKey, String[] uuid);

	Collection<AbstractObject> collection(String resourceKey, String searchField, String searchValue);

	<T extends UUIDDocument> T objectFromReference(UUIDReference ref, Class<T> clz);

	<T extends UUIDDocument> Collection<T> collectionFromReferences(Collection<UUIDReference> refs, Class<T> clz);

	long countObjectsNoScope(String resourceKey, SearchField... fields);

	Collection<AbstractObject> tableObjectsNoScope(String resourceKey, int offset, int limit, String sortColumn,
			SortOrder order, SearchField... fields);

	
}

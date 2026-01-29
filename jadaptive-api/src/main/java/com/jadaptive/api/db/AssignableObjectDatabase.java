package com.jadaptive.api.db;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.jadaptive.api.repository.AssignableDocument;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.user.User;

public interface AssignableObjectDatabase<T extends AssignableDocument> {

	@Deprecated(forRemoval = true, since = "0.6.0")
	Iterable<T> getAssignedObjects(Class<T> resourceClass, User user, SearchField... fields);
	
	@Deprecated(forRemoval = true, since = "0.6.0")
	Iterable<T> getAssignedObjects(Class<T> resourceClass, User user, SortOrder order, String sortField, SearchField... fields);

	T getObjectByUUID(Class<T> resourceClass, String uuid);

	void saveOrUpdate(T obj);

	void deleteObject(T virtualFolder);

	@Deprecated(forRemoval = true, since = "0.6.0")
	T getAssignedObject(Class<T> resourceClass, User user, SearchField... fields);

	/**
	 * Streeam all, or specific objects.
	 * 
	 * @param class1 type
	 * @param fields search fields, or none for all
	 * @return iterable
	 */
	default Stream<T> streamObjects(Class<T> class1, SearchField... fields) {
		return StreamSupport.stream(getObjects(class1, fields).spliterator(), false);
	}

	/**
	 * Get all, or specific objects.
	 * 
	 * @param class1 type
	 * @param fields search fields, or none for all
	 * @return iterable
	 */
	Iterable<T> getObjects(Class<T> class1, SearchField... fields);

	T getObject(Class<T> resourceClass, SearchField... fields);

	long countObjects(Class<T> resourceClass, SearchField... fields);

	Iterable<T> getAssignedObjectsA(Class<T> resourceClass, User user, SearchField... fields);

	T max(Class<T> clz, String column);

	Iterable<T> getAssignedObjectsA(Class<T> resourceClass, User user, SortOrder order, String sortField,
			SearchField... fields);
}

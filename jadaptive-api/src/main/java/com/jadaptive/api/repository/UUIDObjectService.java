package com.jadaptive.api.repository;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.SortOrder;

public interface UUIDObjectService<T extends UUIDDocument> {

	T getObjectByUUID(String uuid);

	String saveOrUpdate(T object);

	void deleteObject(T object);
	
	void deleteObjectByUUID(String uuid);
	
	Iterable<T> allObjects(SearchField... fields);
	
	default Stream<T> streamAll(SearchField... fields) {
		return StreamSupport.stream(allObjects(fields).spliterator(), false);
	}

	void deleteAll();

	Iterable<? extends UUIDDocument> searchTable(int start, int length, SortOrder order, String sortField,
			SearchField... fields);
	
	long countTable(SearchField... fields);

	UUIDDocument createNew(ObjectTemplate template);

	default boolean onObjectStashed(T obj) { return false; }

	@Deprecated(since = "0.6.0", forRemoval = true)
	Collection<T> collection(SearchField... fields);

}

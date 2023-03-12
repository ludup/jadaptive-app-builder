package com.jadaptive.api.repository;

import java.util.Collection;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.template.SortOrder;

public interface UUIDObjectService<T extends UUIDDocument> {

	T getObjectByUUID(String uuid);

	String saveOrUpdate(T object);

	void deleteObject(T object);
	
	void deleteObjectByUUID(String uuid);
	
	Iterable<T> allObjects();

	void deleteAll();

	Collection<? extends UUIDDocument> searchTable(int start, int length, SortOrder order, String sortField,
			SearchField... fields);
	
	long countTable(SearchField... fields);

	default UUIDDocument createNew() { return null; };
}

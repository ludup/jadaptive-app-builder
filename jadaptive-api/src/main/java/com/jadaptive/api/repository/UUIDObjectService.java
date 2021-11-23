package com.jadaptive.api.repository;

public interface UUIDObjectService<T extends UUIDDocument> {

	T getObjectByUUID(String uuid);

	String saveOrUpdate(T object);

	void deleteObject(T object);
	
	void deleteObjectByUUID(String uuid);
	
	Iterable<T> allObjects();
}

package com.jadaptive.api.db;

public interface SingletonObjectDatabase<T> {
	
	void saveObject(T object);

	T getObject(Class<T> resourceClass);
}

package com.jadaptive.api.db;

public interface SystemObjectDatabase<T> {

	void saveObject(T object);

	T getObject(Class<T> resourceClass);
}

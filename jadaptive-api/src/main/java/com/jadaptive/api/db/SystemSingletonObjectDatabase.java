package com.jadaptive.api.db;

public interface SystemSingletonObjectDatabase<T> {

	void saveObject(T object);

	T getObject(Class<T> resourceClass);
}

package com.jadaptive.api.db;

public interface ClassLoaderService {

	Class<?> resolveClass(String name) throws ClassNotFoundException;

}

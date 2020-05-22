package com.jadaptive.api.db;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface ClassLoaderService {

	Class<?> resolveClass(String name) throws ClassNotFoundException;

	Collection<Class<?>> resolveAnnotatedClasses(Class<? extends Annotation> clz);

}

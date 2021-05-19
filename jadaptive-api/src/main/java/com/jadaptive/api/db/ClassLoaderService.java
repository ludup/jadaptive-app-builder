package com.jadaptive.api.db;

import java.lang.annotation.Annotation;
import java.util.Collection;

import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectTemplate;

public interface ClassLoaderService {

	Class<?> findClass(String name) throws ClassNotFoundException;

	Collection<Class<?>> resolveAnnotatedClasses(Class<? extends Annotation> clz);

	ClassLoader getClassLoader();

	Class<? extends UUIDDocument> getTemplateClass(ObjectTemplate template);

	Class<?> resolveClass(String name);

}

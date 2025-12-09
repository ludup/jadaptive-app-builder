package com.jadaptive.api.db;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;

import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectTemplate;

public interface ClassLoaderService {

	Class<?> findClass(String name) throws ClassNotFoundException;

	Collection<Class<?>> resolveAnnotatedClasses(Class<? extends Annotation> clz);

	ClassLoader getClassLoader();

	Class<? extends UUIDDocument> getTemplateClass(ObjectTemplate template);

	boolean hasTemplateClass(ObjectTemplate template);

	URL getResource(String string);

	Class<?> injectClass(ObjectTemplate template);

	Class<?> injectClass(ObjectTemplate template, byte[] bytecode);
	
	Enumeration<URL> findResources(String name) throws IOException;

}

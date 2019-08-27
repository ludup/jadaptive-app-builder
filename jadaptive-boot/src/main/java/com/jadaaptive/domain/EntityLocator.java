package com.jadaaptive.domain;

import java.lang.reflect.Proxy;

public class EntityLocator {

	@SuppressWarnings("unchecked")
	public static <T> T get(ClassLoader classLoader, Class<?>...classes) {
		return (T) Proxy.newProxyInstance(classLoader, 
				classes, new EntityInvocationHandler());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Class<?>...classes) {
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), 
				classes, new EntityInvocationHandler());
	}

}

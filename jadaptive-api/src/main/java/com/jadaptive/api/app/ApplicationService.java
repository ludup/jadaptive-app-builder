package com.jadaptive.api.app;

import java.util.Collection;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

public interface ApplicationService {

	<E> E getBean(Class<E> clz);

	void registerTestingBean(Class<?> clz, Object obj);

	<E> Collection<E> getBeans(Class<E> clz);

	Class<?> resolveClass(String type) throws ClassNotFoundException;

//	AutowireCapableBeanFactory getAutowireCapableBeanFactory();

	<T> T autowire(T obj);

}

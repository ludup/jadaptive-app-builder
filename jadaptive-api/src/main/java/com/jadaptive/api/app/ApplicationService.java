package com.jadaptive.api.app;

import java.util.Collection;

import com.jadaptive.api.repository.SingletonUUIDEntity;

public interface ApplicationService {

	<E> E getBean(Class<E> clz);
	
	void registerTestingBean(Class<?> clz, Object obj);

	<E> Collection<E> getBeans(Class<E> clz);

	Class<?> resolveClass(String type) throws ClassNotFoundException;

	<T> T autowire(T obj);

	static <T extends SingletonUUIDEntity> T getConfig(Class<T> clz) {
		return ApplicationServiceImpl.getInstance().getBean(clz);
	}
}

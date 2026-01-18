package com.jadaptive.api.app;

import java.util.Collection;

public interface J {

	static <T> Collection<T> ls(Class<T> clz) {
		return ApplicationServiceImpl.getInstance().getBeans(clz);
	}
	
	static <T> T b(Class<T> clz) {
		return ApplicationServiceImpl.getInstance().getBean(clz);
	}
	
	static<T> T w(T obj) {
		return ApplicationServiceImpl.getInstance().autowire(obj);
	}
}

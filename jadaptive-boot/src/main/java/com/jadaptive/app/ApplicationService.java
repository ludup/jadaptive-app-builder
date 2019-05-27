package com.jadaptive.app;

import org.springframework.context.ApplicationContext;

public interface ApplicationService {

	ApplicationContext getContext();

	<E> E getBean(Class<E> clz);

}

package com.jadaptive.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

	@Autowired
	ApplicationContext context;
	
	static ApplicationServiceImpl instance = new ApplicationServiceImpl();
	
	Map<Class<?>,Object> testingBeans = new HashMap<>();
	@PostConstruct
	private void postConstruct() {
		instance = this;
	}
	
	@Override
	public ApplicationContext getContext() {
		return context;
	}

	public static ApplicationService getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getBean(Class<E> clz) {
		if(Objects.nonNull(context)) {
			return context.getBean(clz);
		} else {
			Object obj = testingBeans.get(clz);
			if(Objects.isNull(obj)) {
				throw new IllegalStateException("Uninitialized testing bean " + clz.getName());
			}
			return (E)obj;
		}
	}
	
	@Override
	public void registerTestingBean(Class<?> clz, Object obj) {
		testingBeans.put(clz, obj);
	}
}

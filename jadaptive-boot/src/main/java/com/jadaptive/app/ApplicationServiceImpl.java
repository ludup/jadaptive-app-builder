package com.jadaptive.app;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ApplicationServiceImpl implements ApplicationService {

	@Autowired
	ApplicationContext context;
	
	static ApplicationServiceImpl instance;
	
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

	@Override
	public <E> E getBean(Class<E> clz) {
		return context.getBean(clz);
	}
}

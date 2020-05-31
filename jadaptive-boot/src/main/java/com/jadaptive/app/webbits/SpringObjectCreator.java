package com.jadaptive.app.webbits;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.DefaultObjectCreator;
import com.jadaptive.api.app.ApplicationService;

@Component
public class SpringObjectCreator extends DefaultObjectCreator {

	@Autowired
	private ApplicationService applicationService;
	

    @Override
    public <T> T create(Class<T> clazz, Object parent) {
	    try {
	    	return applicationService.getBean(clazz);
	    } catch (NoSuchBeanDefinitionException nsbde) {
	    	T obj = super.create(clazz, parent);
			return applicationService.autowire(obj);
	    }
    }

}

package com.jadaptive.app.webbits;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codesmith.webbits.ObjectCreator;
import com.jadaptive.api.app.ApplicationService;

@Component
public class SpringObjectCreator implements ObjectCreator {

	@Autowired
	private ApplicationService applicationService;
	

    @Override
    public <T> T create(Class<T> clazz, Object parent) {
//	if (parent == null) {
	    try {
		T o = applicationService.getBean(clazz);
		return o;
	    } catch (NoSuchBeanDefinitionException nsbde) {
	    	T obj;
			try {
				obj = clazz.newInstance();
				return applicationService.autowire(obj);
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	    	
	    }
//	} else {
//	    // TODO does this work?
//	    T o = context.getBean(clazz, parent);
//	    return o;
//	}
    }

}

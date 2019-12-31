package com.jadaptive.plugins.example;

import java.util.Arrays;
import java.util.List;

import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.jadaptive.api.ControllerConfiguration;

public class ExamplePlugin extends SpringPlugin implements ControllerConfiguration {

	ApplicationContext applicationContext; 
	
	public ExamplePlugin(PluginWrapper wrapper) {
		super(wrapper);
	}
	
	@Override
	public void stop() {
		System.out.println("Stopping Example Plugin");
	}

	@Override
	public void start() throws PluginException {
		System.out.println("Starting Example Plugin");
		super.start();
	}

	@Override
	public List<?> mvcControllers() {
		return Arrays.asList(new ExampleController());
	}

    @Override
    protected ApplicationContext createApplicationContext() {
    	System.out.println("Creating Example Application Context");
    	if(applicationContext==null) {
    		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
	        applicationContext.setClassLoader(getWrapper().getPluginClassLoader());
	        applicationContext.register(PluginSpringConfiguration.class);
	        applicationContext.refresh();
	        
	        this.applicationContext = applicationContext;
    	}
        return applicationContext;
    }

}

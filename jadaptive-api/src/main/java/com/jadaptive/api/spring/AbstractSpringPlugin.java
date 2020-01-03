package com.jadaptive.api.spring;
import javax.servlet.ServletException;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AbstractSpringPlugin extends SpringPlugin {

	AnnotationConfigApplicationContext pluginContext; 
	
	public AbstractSpringPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {

		try {
			this.pluginContext = createPluginContext();
		} catch (ServletException e) {
			throw new IllegalStateException(e.getMessage(),  e);
		}
		
		doStart();
		super.start();
	}

    protected void doStart() {
		
	}

	@Override
    protected ApplicationContext createApplicationContext() {
    	return pluginContext;
    }

	private AnnotationConfigApplicationContext createPluginContext() throws ServletException {
		
		
		ApplicationContext parentContext = ((SpringPluginManager)getWrapper().getPluginManager()).getApplicationContext();
		
		AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext();
		pluginContext.setParent(parentContext);
		pluginContext.setClassLoader(getWrapper().getPluginClassLoader());
		pluginContext.scan(getClass().getPackage().getName());
		pluginContext.refresh();
       
        return pluginContext;
	}

}

package com.jadaptive.api.spring;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AbstractSpringPlugin extends SpringPlugin {

	static Logger log = LoggerFactory.getLogger(AbstractSpringPlugin.class);
	
	public AbstractSpringPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() {
		doStart();
		super.start();
	}

    protected void doStart() {
		
	}

	@Override
    protected ApplicationContext createApplicationContext() {
	
		if(log.isInfoEnabled()) {
			log.info("Creating application context for {}", getWrapper().getPluginId());
		}
		
		ApplicationContext parentContext = ((SpringPluginManager)getWrapper().getPluginManager()).getApplicationContext();
		
		AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext();
		pluginContext.setParent(parentContext);
		pluginContext.setClassLoader(getWrapper().getPluginClassLoader());
		pluginContext.scan(getClass().getPackage().getName());
		pluginContext.refresh();
       
        return pluginContext;
	}

}

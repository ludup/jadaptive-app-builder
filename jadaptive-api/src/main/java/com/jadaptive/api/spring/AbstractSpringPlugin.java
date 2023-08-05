package com.jadaptive.api.spring;
import java.util.ArrayList;
import java.util.List;

import org.pf4j.PluginDependency;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AbstractSpringPlugin extends SpringPlugin {

	static Logger log = LoggerFactory.getLogger(AbstractSpringPlugin.class);
	
	PluginWrapper wrapper;
	public AbstractSpringPlugin(PluginWrapper wrapper) {
		super(wrapper);
		this.wrapper = wrapper;
	}

	@Override
	public void start() {
		beforeStart();
		super.start();
		afterStart();
	}

    protected void beforeStart() {
		
	}
    
    protected void afterStart() {
    	
    }

	@Override
    protected ApplicationContext createApplicationContext() {
	
		ApplicationContext parentContext = ((SpringPluginManager)wrapper.getPluginManager()).getApplicationContext();

		List<ApplicationContext> parentContexts = new ArrayList<>();
		
		/**
		 * Try to get a single Spring parent dependency context as parent instead
		 */
		for(PluginDependency depend : wrapper.getDescriptor().getDependencies()) {

			PluginWrapper dependWrapper = wrapper.getPluginManager().getPlugin(depend.getPluginId());
			
			if(dependWrapper==null) {
				throw new IllegalStateException("Invalid plugin id " + depend.getPluginId());
			}
			
			if(log.isInfoEnabled()) {
				log.info("Checking dependency {} for Springness", dependWrapper.getPluginId());
			}
			
			if(dependWrapper.getPlugin() instanceof SpringPlugin) {
				if(log.isInfoEnabled()) {
					log.info("Plugin {} has a Spring parent context from {}",
							wrapper.getPluginId(), 
							dependWrapper.getPluginId());
				}
				ApplicationContext ctx = ((SpringPlugin)dependWrapper.getPlugin()).getApplicationContext();
				if(parentContexts.isEmpty()) {
					parentContext = ctx;
				}
				parentContexts.add(ctx);
			}
		}
		
		if(log.isInfoEnabled()) {
			log.info("Creating application context for {}", wrapper.getPluginId());
		}
		
		AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext();
		
		pluginContext.setParent(parentContext);
		pluginContext.setClassLoader(wrapper.getPluginClassLoader());
		pluginContext.scan(getBasePackages());
		pluginContext.refresh();
      
		for(String name : pluginContext.getBeanDefinitionNames()) {
			Object bean = pluginContext.getBean(name);
			ExtensionAutowireHelper.autowiredExtensions(bean, parentContexts);
		}

        return pluginContext;
	}

	protected String[] getBasePackages() {
		return new String[] { getClass().getPackage().getName() };
	}

}

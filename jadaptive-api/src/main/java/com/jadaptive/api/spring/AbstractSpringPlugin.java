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
							getWrapper().getPluginId(), 
							dependWrapper.getPluginId());
				}
				parentContexts.add(parentContext = ((SpringPlugin)dependWrapper.getPlugin()).getApplicationContext());
				break;
			}
		}
		
		AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext();
		pluginContext.setParent(parentContext);
		pluginContext.setClassLoader(getWrapper().getPluginClassLoader());
		pluginContext.scan(getBasePackages());
		pluginContext.refresh();
      
        return pluginContext;
	}

	protected String[] getBasePackages() {
		return new String[] { getClass().getPackage().getName() };
	}

}

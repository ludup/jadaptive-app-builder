package com.jadaptive.app;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.jadaptive.api.ControllerConfiguration;

@Configuration
public class PluginConfig implements BeanFactoryAware {


    private final SpringPluginManager pluginManager;
    private final ApplicationContext applicationContext;
    private BeanFactory beanFactory;

    @Autowired
    public PluginConfig(SpringPluginManager pm, ApplicationContext applicationContext) {
        this.pluginManager = pm;
        this.applicationContext = applicationContext;
    }
    
    @PostConstruct 
    private void postConstruct() {
    	registerMvcEndpoints(pluginManager);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private void registerMvcEndpoints(PluginManager pm) {
    	
    	
    	for(PluginWrapper p : pm.getPlugins()) {
    		if(p.getPlugin() instanceof ControllerConfiguration) {
    			for(Object c : ((ControllerConfiguration)p.getPlugin()).mvcControllers()) {
    				((ConfigurableBeanFactory) beanFactory).registerSingleton(c.getClass().getName(), c);
    			}
    		}
    	}
    	
		applicationContext.getBeansOfType(RequestMappingHandlerMapping.class)
    		.forEach((k, v) -> v.afterPropertiesSet());
    }
    
    @PreDestroy
    public void cleanup() {
        pluginManager.stopPlugins();
    }
}
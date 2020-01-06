package com.jadaptive.app;

import javax.annotation.PreDestroy;

import org.pf4j.ExtensionFactory;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	SpringPluginManager pluginManager;
	
	@Autowired
	ApplicationContext applicationContext;
	
    @Bean
    public SpringPluginManager pluginManager() {
        pluginManager = new SpringPluginManager() {
        	@Override
            protected ExtensionFactory createExtensionFactory() {
                return new CustomSpringExtensionFactory(this);
            }
        };
        return pluginManager;
    }
    
    @PreDestroy
    public void cleanup() {
        pluginManager.stopPlugins();
    }
}
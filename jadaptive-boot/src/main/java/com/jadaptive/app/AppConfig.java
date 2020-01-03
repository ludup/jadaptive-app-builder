package com.jadaptive.app;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	SpringPluginManager pluginManager;
    @Bean
    public SpringPluginManager pluginManager() {
        return pluginManager = new SpringPluginManager();
    }
    
    @PreDestroy
    public void cleanup() {
        pluginManager.stopPlugins();
    }
}
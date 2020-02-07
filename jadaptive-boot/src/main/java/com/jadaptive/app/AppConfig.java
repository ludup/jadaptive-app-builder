package com.jadaptive.app;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DevelopmentPluginRepository;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginRepository;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.ExtensionsInjector;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.StartupAware;

@Configuration
public class AppConfig {

	static Logger log = LoggerFactory.getLogger(AppConfig.class);
	
	SpringPluginManager pluginManager;
	
	@Autowired
	ApplicationContext applicationContext;
	
    @Bean
    public SpringPluginManager pluginManager() {
        pluginManager = new SpringPluginManager() {
        	
			@Override
			protected ExtensionFinder createExtensionFinder() {
				return new ScanningExtensionFinder(this);
			}

			@Override
            protected ExtensionFactory createExtensionFactory() {
                return new CustomSpringExtensionFactory(this);
            }
        	
        	@Override
            protected PluginRepository createPluginRepository() {
        		
               CompoundPluginRepository pluginRepository = new CompoundPluginRepository();
               
               pluginRepository.add(new DevelopmentPluginRepository(getPluginsRoot()), this::isDevelopment);
               
               String[] additionalDevelopmentPaths = System.getProperty("jadaptive.developmentPluginDirs", "../../jadaptive-plugins").split(",");
               for(String path : additionalDevelopmentPaths) {
            	   pluginRepository.add(new DevelopmentPluginRepository(Paths.get(path)), this::isDevelopment);
               }
               pluginRepository.add(new JarPluginRepository(getPluginsRoot()), this::isNotDevelopment);
               pluginRepository.add(new DefaultPluginRepository(getPluginsRoot()), this::isNotDevelopment);
               
               return pluginRepository;
            }
        };
        return pluginManager;
    }
    
    @PreDestroy
    public void cleanup() {
        pluginManager.stopPlugins();
    }
}
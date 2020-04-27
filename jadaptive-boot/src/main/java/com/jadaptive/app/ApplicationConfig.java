package com.jadaptive.app;

import java.nio.file.Paths;

import javax.annotation.PreDestroy;

import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DevelopmentPluginRepository;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginRepository;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.codesmith.webbits.WebbitsServlet;
import com.jadaptive.app.json.upload.UploadServlet;
import com.jadaptive.app.scheduler.LockableTaskScheduler;

@Configuration
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

	static Logger log = LoggerFactory.getLogger(ApplicationConfig.class);
	
	SpringPluginManager pluginManager;
	
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
               
               String[] additionalDevelopmentPaths = System.getProperty(
            		   "jadaptive.developmentPluginDirs", 
            		   "../../jadaptive-vsftp,../../jadaptive-updates,../../jadaptive-key-server").split(",");
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

    @Bean
    public LockableTaskScheduler taskScheduler() {
        return new LockableTaskScheduler();
    }
    
	@PreDestroy
    public void cleanup() {
        pluginManager.stopPlugins();
    }
	
	@Bean
	public ServletRegistrationBean<?> uploadServletBean() {
	    ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(
	      new UploadServlet(), "/upload/*");
	    bean.setLoadOnStartup(1);
	    return bean;
	}
	
	@Bean
	public ServletRegistrationBean<?> webbitsServletBean() {
	    ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(
	      new WebbitsServlet(), "/ui/*");
	    bean.setLoadOnStartup(1);
	    bean.setAsyncSupported(true);
	    return bean;
	}
}
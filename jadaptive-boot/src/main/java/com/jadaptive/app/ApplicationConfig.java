package com.jadaptive.app;

import java.io.FileFilter;
import java.nio.file.Paths;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DevelopmentPluginRepository;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginRepository;
import org.pf4j.spring.SpringPluginManager;
import org.pf4j.util.NameFileFilter;
import org.pf4j.util.OrFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.jadaptive.app.json.upload.UploadServlet;
import com.jadaptive.app.scheduler.LockableTaskScheduler;

@Configuration
@ComponentScan({"com.jadaptive.app.**", "com.jadaptive.api.**"})
@ServletComponentScan("com.jadaptive.app.**")
@EnableAsync
@EnableScheduling
public class ApplicationConfig {

	static Logger log = LoggerFactory.getLogger(ApplicationConfig.class);
	
	SpringPluginManager pluginManager;
	
	@Autowired
	private ApplicationContext applicationContext;
	
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
               
               pluginRepository.add(new DevelopmentPluginRepository(getPluginsRoot()) {
            	   protected FileFilter createHiddenPluginFilter() {
            	        OrFileFilter hiddenPluginFilter = (OrFileFilter) super.createHiddenPluginFilter();

            	        for(String id : System.getProperty("disabled.plugins", "").split(",")) {
            	        	hiddenPluginFilter.addFileFilter(new NameFileFilter(id));
            	        }
            	        
            	        return hiddenPluginFilter;
            	    }
               }, this::isDevelopment);
               
               String[] additionalDevelopmentPaths = System.getProperty(
            		   "jadaptive.developmentPluginDirs", 
            		   "").split(",");
               
               for(String path : additionalDevelopmentPaths) {
            	   if(StringUtils.isNotBlank(path)) {
            		   pluginRepository.add(new DevelopmentPluginRepository(Paths.get(path)) {
                    	   protected FileFilter createHiddenPluginFilter() {
                   	        OrFileFilter hiddenPluginFilter = (OrFileFilter) super.createHiddenPluginFilter();

                   	        for(String id : System.getProperty("disabled.plugins", "").split(",")) {
                   	        	hiddenPluginFilter.addFileFilter(new NameFileFilter(id));
                   	        }
                   	        
                   	        return hiddenPluginFilter;
                   	    }
                      }, this::isDevelopment);
            	   }
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
		UploadServlet servlet = new UploadServlet();
		applicationContext.getAutowireCapableBeanFactory().autowireBean(servlet);
	    ServletRegistrationBean<?> bean = new ServletRegistrationBean<>(
	      servlet, "/upload/*");
	    bean.setLoadOnStartup(1);
	    return bean;
	}

}
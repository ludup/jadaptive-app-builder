package com.jadaptive.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.spring.AbstractSpringPlugin;

@Service
public class ApplicationServiceImpl implements ApplicationService {

	static Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private SpringPluginManager pluginManager; 
	
	@Autowired
	ClassLoaderService classLoaderService; 
	
	static ApplicationServiceImpl instance = new ApplicationServiceImpl();
	
	Map<Class<?>,Object> testingBeans = new HashMap<>();
	@PostConstruct
	private void postConstruct() {
		instance = this;
	}

	public static ApplicationService getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getBean(Class<E> clz) {
		if(Objects.nonNull(context)) {
			Collection<E> beans = getBeans(clz);
			if(beans.isEmpty()) {
				throw new IllegalStateException("Missing bean of type " + clz.getName());
			}
			if(beans.size() > 1) {
				throw new IllegalStateException("Multiple beans of type " + clz.getName());
			}
			return beans.iterator().next();
		} else {
			Object obj = testingBeans.get(clz);
			if(Objects.isNull(obj)) {
				throw new IllegalStateException("Uninitialized testing bean " + clz.getName());
			}
			return (E)obj;
		}
	}
	
	@Override
	public <E> Collection<E> getBeans(Class<E> clz) {
		
		List<E> results = new ArrayList<>();
		
		for(PluginWrapper w : pluginManager.getPlugins()) {
			
			if(w.getPlugin()==null) {
				continue;
			}
			
			if(w.getPlugin() instanceof SpringPlugin) {
				if(log.isDebugEnabled()) {
					log.debug("Scanning plugin {} for beans {}", 
							w.getPluginId(),
							clz.getName());
				}
			
				Map<String,E> tmp = ((SpringPlugin)w.getPlugin())
						.getApplicationContext().getBeansOfType(clz);
				
				if(log.isDebugEnabled()) {
					log.debug("Found {} plugin beans of type {}", tmp.size(), clz.getName());
				}
				
				results.addAll(tmp.values());
			}
		}
		
		Map<String,E> tmp = context.getBeansOfType(clz);
		
		if(log.isDebugEnabled()) {
			log.debug("Found {} system beans of type {}", tmp.size(), clz.getName());
		}
		
		results.addAll(tmp.values());
		
		return results;
	}
	
	@Override
	public void registerTestingBean(Class<?> clz, Object obj) {
		testingBeans.put(clz, obj);
	}

	@Override
	public Class<?> resolveClass(String type) throws ClassNotFoundException {
		return classLoaderService.resolveClass(type);
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
		return context.getAutowireCapableBeanFactory();
	}
}

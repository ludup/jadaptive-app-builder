package com.jadaptive.api.app;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDependency;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPlugin;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.repository.ReflectionUtils;
import com.jadaptive.api.spring.ExtensionAutowireHelper;

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
				throw new NoSuchBeanDefinitionException("Missing bean of type " + clz.getName());
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
	public <T> T autowire(T obj) {
		
		List<ApplicationContext> dependencyContexts = new ArrayList<>();
		ApplicationContext parentContext = null;
		PluginWrapper pluginWrapper = null;
		try {
			if(obj.getClass().getClassLoader() instanceof PluginClassLoader) {
				PluginClassLoader classLoader = (PluginClassLoader) obj.getClass().getClassLoader();
				for(PluginWrapper w : pluginManager.getPlugins()) {
					if(w.getPluginClassLoader().equals(classLoader)) {
						if(w.getPlugin() instanceof SpringPlugin) {
							SpringPlugin plugin = (SpringPlugin) w.getPlugin();
							pluginWrapper = w;
							parentContext = plugin.getApplicationContext();
							dependencyContexts.add(parentContext);
							break;
						}
					}
				}
			}
			
			if(Objects.nonNull(pluginWrapper)) {
				
				for(PluginDependency dependency : pluginWrapper.getDescriptor().getDependencies()) {
					PluginWrapper w = pluginManager.getPlugin(dependency.getPluginId());
					if(w.getPlugin() instanceof SpringPlugin) {
						SpringPlugin plugin = (SpringPlugin) w.getPlugin();
						dependencyContexts.add(plugin.getApplicationContext());
					}
				}
			}
			
			if(Objects.isNull(parentContext)) {
				parentContext = context;
			}
			
			parentContext.getAutowireCapableBeanFactory().autowireBean(obj);
			ExtensionAutowireHelper.autowiredExtensions(obj, dependencyContexts);
			
			ReflectionUtils.executeAnnotatedMethods(obj, PostConstruct.class);
			return obj;
		
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	@Override
	public void registerTestingBean(Class<?> clz, Object obj) {
		testingBeans.put(clz, obj);
	}

	@Override
	public Class<?> resolveClass(String type) throws ClassNotFoundException {
		return classLoaderService.findClass(type);
	}
//
//	@Override
//	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() {
//		return context.getAutowireCapableBeanFactory();
//	}


}

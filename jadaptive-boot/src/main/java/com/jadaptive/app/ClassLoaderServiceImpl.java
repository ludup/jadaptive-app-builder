package com.jadaptive.app;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.ClassLoaderService;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.ObjectTemplate;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

@Service
public class ClassLoaderServiceImpl extends ClassLoader implements ClassLoaderService {

	static ClassLoaderServiceImpl instance;
	
	static Logger log = LoggerFactory.getLogger(ClassLoaderServiceImpl.class);
	
	@Autowired
	private PluginManager pluginManager; 
	
	@PostConstruct
	private void postConstruct() {
		ClassLoaderServiceImpl.instance = this;
	}
	
	public static ClassLoaderServiceImpl getInstance() {
		return instance;
	}
	
	@Override
	protected URL findResource(String name) {
		
		for(PluginWrapper w : pluginManager.getPlugins()) {
			URL url = w.getPluginClassLoader().getResource(name);
			if(Objects.nonNull(url)) {
				return url;
			}
		}

		return getClass().getClassLoader().getResource(name);
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		return new Enumeration<URL>() {

			Set<URL> tmp = new HashSet<>();
			Iterator<URL> it;
			{
				Enumeration<URL> e;
				for(PluginWrapper w : pluginManager.getPlugins()) {
					e = w.getPluginClassLoader().getResources(name);
					while(e.hasMoreElements()) {
						tmp.add(e.nextElement());
					}
				}
				e = getClass().getClassLoader().getResources(name);
				while(e.hasMoreElements()) {
					tmp.add(e.nextElement());
				}
				it = tmp.iterator();
			}
			@Override
			public boolean hasMoreElements() {
				return it.hasNext();
			}

			@Override
			public URL nextElement() {
				return it.next();
			}
		};
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		
		for(PluginWrapper w : pluginManager.getPlugins()) {
			try {
				return w.getPluginClassLoader().loadClass(name);
			} catch(ClassNotFoundException e) {
			}
		}

		return getClass().getClassLoader().loadClass(name);

	}

	@Override
	public Collection<Class<?>> resolveAnnotatedClasses(Class<? extends Annotation> clz) {
		
		Collection<Class<?>> results = new ArrayList<>();

        List<PluginWrapper> plugins = pluginManager.getPlugins();
        for (PluginWrapper plugin : plugins) {
            if(Objects.nonNull(plugin.getPlugin())) {
            
	            try (ScanResult scanResult =
	                    new ClassGraph()                  
	                        .enableAllInfo()  
	                        .addClassLoader(plugin.getPluginClassLoader())
	                        .whitelistPackages(plugin.getPlugin().getClass().getPackage().getName())   
	                        .scan()) {              
	                for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(clz.getName())) {
						try {
							results.add(plugin.getPluginClassLoader().loadClass(classInfo.getName()));
						} catch (ClassNotFoundException e) {
							log.error("Failed to load annotated class", e);
						}
	                }
	            } catch(Throwable t) {
	            	log.error("Could not process {} type for plugin {}", clz.getSimpleName(), plugin.getPluginId(), t);
	            }
            }
        }
        
        try (ScanResult scanResult =
                new ClassGraph()                   
                    .enableAllInfo()     
                    .addClassLoader(getClass().getClassLoader())
                    .whitelistPackages(Application.class.getPackage().getName())   
                    .scan()) {                  
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(clz.getName())) {
            	try {
            		results.add(getClass().getClassLoader().loadClass(classInfo.getName()));
				} catch (ClassNotFoundException e) {
					log.error("Failed to load annotated class", e);
				}
            }
        } catch(Throwable t) {
        	log.error("Could not process {} type for system classpath", clz.getSimpleName(), t);
        }

        return results;
	}

	@Override
	public ClassLoader getClassLoader() {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends UUIDDocument> getTemplateClass(ObjectTemplate template) {
		try {
			return (Class<? extends UUIDDocument>) findClass(template.getTemplateClass());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public boolean hasTemplateClass(ObjectTemplate template) {
		try {
			findClass(template.getTemplateClass());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}

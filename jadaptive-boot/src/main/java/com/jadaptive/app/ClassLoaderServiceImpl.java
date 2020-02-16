package com.jadaptive.app;

import javax.annotation.PostConstruct;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.ClassLoaderService;

@Service
public class ClassLoaderServiceImpl implements ClassLoaderService {

	static ClassLoaderServiceImpl instance;
	
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
	public Class<?> resolveClass(String name) throws ClassNotFoundException {
		
		for(PluginWrapper w : pluginManager.getPlugins()) {
			try {
				return w.getPluginClassLoader().loadClass(name);
			} catch(ClassNotFoundException e) {
			}
		}

		return getClass().getClassLoader().loadClass(name);

	}
}

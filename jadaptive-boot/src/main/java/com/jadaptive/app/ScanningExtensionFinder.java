package com.jadaptive.app;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.pf4j.AbstractExtensionFinder;
import org.pf4j.Extension;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class ScanningExtensionFinder extends AbstractExtensionFinder {

	static Logger log = LoggerFactory.getLogger(ScanningExtensionFinder.class);
	
	public ScanningExtensionFinder(PluginManager pluginManager) {
		super(pluginManager);
	}

	@Override
	public Map<String, Set<String>> readPluginsStorages() {
		log.debug("Reading extensions storages from plugins");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        List<PluginWrapper> plugins = pluginManager.getPlugins();
        for (PluginWrapper plugin : plugins) {
            String pluginId = plugin.getDescriptor().getPluginId();
            log.debug("Reading extensions storage from plugin '{}'", pluginId);
            Set<String> bucket = new HashSet<>();
            if(Objects.nonNull(plugin.getPlugin())) {
            
	            try (ScanResult scanResult =
	                    new ClassGraph()                  
	                        .enableAllInfo()  
	                        .addClassLoader(plugin.getPluginClassLoader())
	                        .whitelistPackages(plugin.getPlugin().getClass().getPackage().getName())   
	                        .scan()) {              
	                for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(Extension.class.getName())) {
	                    if(log.isInfoEnabled()) {
							log.info("Found extension {}", classInfo.getName());
						}
						bucket.add(classInfo.getName());
	                }
	            }
            }

			debugExtensions(bucket);

			result.put(pluginId, bucket);
        }

        return result;
	}

	@Override
	public Map<String, Set<String>> readClasspathStorages() {
		log.debug("Reading extensions storages from classpath");
        Map<String, Set<String>> result = new LinkedHashMap<>();

        Set<String> bucket = new HashSet<>();
        
        try (ScanResult scanResult =
                new ClassGraph()                   
                    .enableAllInfo()     
                    .addClassLoader(getClass().getClassLoader())
                    .whitelistPackages(Application.class.getPackage().getName())   
                    .scan()) {                  
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(Extension.class.getName())) {
                
                if(log.isInfoEnabled()) {
					log.info("Found extension {}", classInfo.getName());
				}
				bucket.add(classInfo.getName());
            }
        }

        debugExtensions(bucket);

        result.put(null, bucket);


        return result;
	}


}

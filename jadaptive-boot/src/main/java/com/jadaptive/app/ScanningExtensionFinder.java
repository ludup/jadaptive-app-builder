package com.jadaptive.app;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pf4j.AbstractExtensionFinder;
import org.pf4j.Extension;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				
				builder.addClassLoaders(plugin.getPluginClassLoader());
				builder.addUrls(ClasspathHelper.forPackage(
						plugin.getPlugin().getClass().getPackage().getName(),
						plugin.getPluginClassLoader()));
				builder.addScanners(new TypeAnnotationsScanner());

				Reflections reflections = new Reflections(builder);
				
				for(Class<?> clz : reflections.getTypesAnnotatedWith(Extension.class)) {
					if(log.isInfoEnabled()) {
						log.info("Found extension {}", clz.getName());
					}
					bucket.add(clz.getName());
				}
			} catch (Exception e) {
				log.error("Failed to look up Extensions");
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

    	try {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			
			builder.addClassLoaders(getClass().getClassLoader());
			builder.addUrls(ClasspathHelper.forPackage(Application.class.getPackage().getName()));
			builder.addScanners(new TypeAnnotationsScanner());

			Reflections reflections = new Reflections(builder);
			
			for(Class<?> clz : reflections.getTypesAnnotatedWith(Extension.class)) {
				if(log.isInfoEnabled()) {
					log.info("Found extension {}", clz.getName());
				}
				bucket.add(clz.getName());
			}
		} catch (Exception e) {
			log.error("Failed to look up Extensions", e);
		}

        debugExtensions(bucket);

        result.put(null, bucket);


        return result;
	}


}

package com.jadaptive.app;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.annotation.PreDestroy;

import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DevelopmentPluginRepository;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginRepository;
import org.pf4j.PluginRuntimeException;
import org.pf4j.spring.SpringPluginManager;
import org.pf4j.update.FileDownloader;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.PluginInfo.PluginRelease;
import org.pf4j.update.UpdateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jadaptive.utils.Version;

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
               
               String[] additionalDevelopmentPaths = System.getProperty(
            		   "jadaptive.developmentPluginDirs", 
            		   "../../jadaptive-vsftp,../../jadaptive-updates").split(",");
               for(String path : additionalDevelopmentPaths) {
            	   pluginRepository.add(new DevelopmentPluginRepository(Paths.get(path)), this::isDevelopment);
               }
               pluginRepository.add(new JarPluginRepository(getPluginsRoot()), this::isNotDevelopment);
               pluginRepository.add(new DefaultPluginRepository(getPluginsRoot()), this::isNotDevelopment);
               
               return pluginRepository;
            }

			@Override
			public void loadPlugins() {
				super.loadPlugins();
				
		        checkForUpdates();
			}
        	
        	
        };
        
        
        return pluginManager;
    }
    
    private void checkForUpdates() {
		
    	File repositories = new File("conf/repositories.json");
    	
    	if(repositories.exists()) {
	    	UpdateManager updateManager = new UpdateManager(pluginManager,
	    			repositories.toPath()) {
	
				@Override
				protected FileDownloader getFileDownloader(String pluginId) {
					return new SerialReportingFileDownloader();
				}
						
				public boolean hasPluginUpdate(String id) {
					 PluginInfo pluginInfo = getPluginsMap().get(id);
				        if (pluginInfo == null) {
				            return false;
				        }
				        
				        Version installedVersion = new Version(
				        	pluginManager.getPlugin(id)
				        		.getDescriptor().getVersion());
				        
				        PluginRelease last = getLastPluginRelease(id);
				        if(last!=null) {
				        	Version lastVersion = new Version(last.version);
				        	return lastVersion.compareTo(installedVersion) > 0
				        			|| (installedVersion.isSnapshot() 
				        					&& lastVersion.isSnapshot());
				        } 
				        
				        return false;
			    }
	    		
	    	};
	    	
	    	// >> keep system up-to-date <<
	        boolean systemUpToDate = true;
	
	        // check for updates
	        if (updateManager.hasUpdates()) {
	            List<PluginInfo> updates = updateManager.getUpdates();
	            log.debug("Found {} updates", updates.size());
	            for (PluginInfo plugin : updates) {
	                log.debug("Found update for plugin '{}'", plugin.id);
	                PluginInfo.PluginRelease lastRelease = updateManager.getLastPluginRelease(plugin.id);
	                String lastVersion = lastRelease.version;
	                String installedVersion = pluginManager.getPlugin(plugin.id).getDescriptor().getVersion();
	                log.debug("Update plugin '{}' from version {} to version {}", plugin.id, installedVersion, lastVersion);
	                boolean updated = updateManager.updatePlugin(plugin.id, lastVersion);
	                if (updated) {
	                    log.debug("Updated plugin '{}'", plugin.id);
	                } else {
	                    log.error("Cannot update plugin '{}'", plugin.id);
	                    systemUpToDate = false;
	                }
	            }
	        } else {
	            log.debug("No updates found");
	        }
	
	        // check for available (new) plugins
	        if (updateManager.hasAvailablePlugins()) {
	            List<PluginInfo> availablePlugins = updateManager.getAvailablePlugins();
	            log.debug("Found {} available plugins", availablePlugins.size());
	            for (PluginInfo plugin : availablePlugins) {
	                log.debug("Found available plugin '{}'", plugin.id);
	                PluginInfo.PluginRelease lastRelease = updateManager.getLastPluginRelease(plugin.id);
	                String lastVersion = lastRelease.version;
	                log.debug("Install plugin '{}' with version {}", plugin.id, lastVersion);
	                boolean installed = updateManager.installPlugin(plugin.id, lastVersion);
	                if (installed) {
	                    log.debug("Installed plugin '{}'", plugin.id);
	                } else {
	                    log.error("Cannot install plugin '{}'", plugin.id);
	                    systemUpToDate = false;
	                }
	            }
	        } else {
	            log.debug("No available plugins found");
	        }
	
	        if (systemUpToDate) {
	            log.debug("System up-to-date");
	        }
    	}
		
	}

	@PreDestroy
    public void cleanup() {
        pluginManager.stopPlugins();
    }
	
	
	class SerialReportingFileDownloader implements FileDownloader {

		@Override
		public Path downloadFile(URL url) throws IOException {
			
			if(log.isInfoEnabled()) {
				log.info("Downloading {}", url.toExternalForm());
			}
			
			Path destination = Files.createTempDirectory("pf4j-update-downloader");
	        destination.toFile().deleteOnExit();
	        
			if(url.getProtocol().startsWith("http")) {
				HttpURLConnection hc = (HttpURLConnection) url.openConnection();
	
		        hc.setDoOutput(false);
		        hc.setDoInput(true);
		        hc.setUseCaches(false);
	
		        hc.setRequestProperty("Product-Serial", ApplicationVersion.getSerial());
		        hc.setRequestProperty("Product-Id", ApplicationVersion.getProductId());
		        hc.setRequestProperty("Product-Version", ApplicationVersion.getVersion());
			       
		        hc.connect();
		        
		        String path = url.getPath();
		        String fileName = path.substring(path.lastIndexOf('/') + 1);
		        Path toFile = destination.resolve(fileName);
		        
		        Files.copy(hc.getInputStream(), toFile, StandardCopyOption.REPLACE_EXISTING);
		        return toFile;
			} else {
				try {
					Path fromFile = Paths.get(url.toURI());
		            String path = url.getPath();
		            String fileName = path.substring(path.lastIndexOf('/') + 1);
		            Path toFile = destination.resolve(fileName);
		            Files.copy(fromFile, toFile, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		            return toFile;
				} catch (URISyntaxException e) {
		            throw new PluginRuntimeException("Something wrong with given URL", e);
		        }
			}
		    
		}
		
	}
}
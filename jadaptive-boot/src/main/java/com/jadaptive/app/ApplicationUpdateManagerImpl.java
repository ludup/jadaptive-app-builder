package com.jadaptive.app;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.update.FileDownloader;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.PluginInfo.PluginRelease;
import org.pf4j.update.UpdateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationUpdateManager;
import com.jadaptive.utils.Version;

@Service
public class ApplicationUpdateManagerImpl extends UpdateManager implements ApplicationUpdateManager {

	static Logger log = LoggerFactory.getLogger(ApplicationUpdateManagerImpl.class);
		
	private PluginManager pluginManager;

	public ApplicationUpdateManagerImpl(PluginManager pluginManager) {
		super(pluginManager, new File("conf/repositories.json").toPath());
		this.pluginManager = pluginManager;
	}

	@Override
	protected FileDownloader getFileDownloader(String pluginId) {
		return new SerialReportingFileDownloader();
	}
			
	public boolean hasPluginUpdate(String id) {
		PluginInfo pluginInfo = getPluginsMap().get(id);
        if (pluginInfo == null) {
            return false;
        }
        
        PluginWrapper wrapper = pluginManager.getPlugin(id);
        Version installedVersion = new Version(
        	wrapper.getDescriptor().getVersion());
        
        PluginRelease latestRelease = getLastPluginRelease(id);
        if(latestRelease!=null) {
        	Version latestVersion = new Version(latestRelease.version);
        	return checkPluginVersion(installedVersion, latestVersion,
        				latestRelease, wrapper.getPluginId(), wrapper.getPluginPath());
        		
        } 
        
        return false;
    }
	
	private boolean checkPluginVersion(Version installedVersion, 
			Version latestVersion,
			PluginInfo.PluginRelease latestRelease,
			String pluginId,
			Path pluginPath) {
		if(latestVersion.compareTo(installedVersion) > 0) {
    		log.info("Found updated version {} for {} version {}", 
    				latestRelease.version, pluginId, installedVersion.toString());
    		return true;
    	}
		
		if(installedVersion.isSnapshot() && latestVersion.equals(installedVersion)) {
    		try {
				if(latestRelease.date.toInstant().isAfter(
						Files.getLastModifiedTime(pluginPath).toInstant())) {
							log.info("Found updated SNAPSHOT version for {} version {}", 
				    				pluginId, installedVersion.toString());
					return true;
				}
			} catch (IOException e) {
				log.error("Could not resolve last modified time of plugin path", e);
			}
    	}
		return false;
	}
	
	public boolean processPlugin(PluginInfo plugin, PluginInfo.PluginRelease lastRelease) {
		log.debug("Found available plugin '{}'", plugin.id);

        String lastVersion = lastRelease.version;
        log.debug("Install plugin '{}' with version {}", plugin.id, lastVersion);
        boolean installed = installPluginFile(plugin.id, lastVersion, lastRelease.date);
        if (installed) {
            log.debug("Installed plugin '{}'", plugin.id);
            return true;
        } else {
            log.error("Cannot install plugin '{}'", plugin.id);
            return false;
        }
	}

	private boolean installPluginFile(String id, String version, Date date) {
        Path downloaded = downloadPlugin(id, version);

        Path pluginsRoot = pluginManager.getPluginsRoot();
        Path file = pluginsRoot.resolve(downloaded.getFileName());
        try {
            Files.move(downloaded, file, REPLACE_EXISTING);
            Files.setLastModifiedTime(file, FileTime.fromMillis(date.getTime()));
            return true;
        } catch (IOException e) {
            throw new PluginRuntimeException(e, "Failed to write file '{}' to plugins folder", file);
        }
	}

	public boolean processApplicationPlugin(PluginInfo plugin, PluginInfo.PluginRelease lastRelease) {
		
		log.debug("Install application core '{}' with version {}", plugin.id, lastRelease.version);
		
		try {
			File destination = new File("app");
			FileUtils.deleteDirectory(destination);
			destination.mkdirs();
			
			Path tmp = downloadPlugin(plugin.id, lastRelease.version);
			String fileName = lastRelease.url.substring(lastRelease.url.lastIndexOf('/') + 1);
			File appjar = new File(destination, fileName);
			Files.copy(tmp, appjar.toPath());
			Files.setLastModifiedTime(appjar.toPath(), FileTime.fromMillis(lastRelease.date.getTime()));
			return true;
		} catch (IOException e) {
			log.error("Failed to update application jar file", e);
			return false;
		}
		
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

	@Override
	public void check4Updates(List<PluginInfo> toUpdate, List<PluginInfo> toInstall) {
		
		refresh();

        // check for updates
        if (hasUpdates()) {
            List<PluginInfo> updates = getUpdates();
            log.debug("Found {} updates", updates.size());
            for (PluginInfo plugin : updates) {
                log.debug("Found update for plugin '{}'", plugin.id);
                toUpdate.add(plugin);
            }
        } else {
            log.debug("No updates found");
        }

        if (hasAvailablePlugins()) {
            List<PluginInfo> availablePlugins = getAvailablePlugins();
            log.debug("Found {} available plugins", availablePlugins.size());
            for (PluginInfo plugin : availablePlugins) {
	            if(plugin.id.equals("jadaptive-boot")) {
	            	Version installedVersion = new Version(ApplicationVersion.getVersion());
	            	PluginInfo.PluginRelease release = getLastPluginRelease(plugin.id);
	            	
	            	log.debug("Checking core application version {} against {}", 
	            			ApplicationVersion.getVersion(),
	            			release.version);
	            	
	            	Version latestVersion = new Version(release.version);
	            	if(!checkPluginVersion(installedVersion, latestVersion, release, plugin.id,
	            		new File("app/jadaptive-boot-" + installedVersion.toString() + ".jar").toPath())) {
	            		continue;
	            	}
	            } 
	            toInstall.add(plugin);
	            
            }
        } else {
            log.debug("No available plugins found");
        }

	}
	
	@Override
	public boolean installUpdates() throws IOException {
		List<PluginInfo> toUpdate = new ArrayList<>();
		List<PluginInfo> toInstall = new ArrayList<>();
		
		check4Updates(toUpdate, toInstall);
		
		if(!toUpdate.isEmpty() || !toInstall.isEmpty()) {
			return installUpdates(toUpdate, toInstall);
		}
		
		return false;
	}
	
	@Override
	public boolean installUpdates(List<PluginInfo> toUpdate, List<PluginInfo> toInstall) throws IOException {
	    
        for (PluginInfo plugin : toUpdate) {
            log.debug("Found update for plugin '{}'", plugin.id);
            PluginInfo.PluginRelease lastRelease = getLastPluginRelease(plugin.id);
            String lastVersion = lastRelease.version;
            PluginWrapper wrapper = pluginManager.getPlugin(plugin.id);
            String installedVersion = wrapper.getDescriptor().getVersion();
            log.debug("Update plugin '{}' from version {} to version {}", plugin.id, installedVersion, lastVersion);

            boolean updated = processPlugin(plugin, lastRelease);
            if (updated) {
                log.debug("Updated plugin '{}'", plugin.id);
            } else {
                log.error("Cannot update plugin '{}'", plugin.id);
            }
        	
        }
	
        for (PluginInfo plugin : toInstall) {
        	
        	PluginInfo.PluginRelease lastRelease = getLastPluginRelease(plugin.id);
        	
        	log.debug("Installing plugin '{} from {}'", plugin.id, plugin.projectUrl);
        	
        	if("jadaptive-boot".equals(plugin.id)) {
        		if(!processApplicationPlugin(plugin, lastRelease)) {
        			throw new IOException("Failed to install " + plugin.id);
        		}
        	} else {
        		boolean installed = processPlugin(plugin, lastRelease);
	            if (installed) {
	                log.debug("Installed plugin '{}'", plugin.id);
	            } else {
	                log.error("Cannot install plugin '{}'", plugin.id);
	            }
        	}
        }
        
        return true;
	}
}

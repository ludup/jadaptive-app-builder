package com.jadaptive.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.update.DefaultUpdateRepository;
import org.pf4j.update.FileDownloader;
import org.pf4j.update.FileVerifier;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.PluginInfo.PluginRelease;
import org.pf4j.update.UpdateManager;
import org.pf4j.update.UpdateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationUpdateManager;
import com.jadaptive.utils.Version;

@Service
public class ApplicationUpdateManagerImpl extends UpdateManager implements ApplicationUpdateManager {

	static Logger log = LoggerFactory.getLogger(ApplicationUpdateManagerImpl.class);
		
	private PluginManager pluginManager;

	private boolean hasUpdates = false;
	
	@Autowired
	ApplicationContext applicationContext;
	
	public ApplicationUpdateManagerImpl(PluginManager pluginManager) {
		super(pluginManager, new File("conf/repositories.json").toPath());
		this.pluginManager = pluginManager;
	}

	@Override
	protected FileDownloader getFileDownloader(String pluginId) {
		return new SerialReportingFileDownloader();
	}
	
    public void addRepository(String id, URL url) {
        for (UpdateRepository ur : repositories) {
            if (ur.getId().equals(id)) {
                throw new RuntimeException("Repository with id " + id + " already exists");
            }
        }
        repositories.add(new DefaultUpdateRepository(id, url) {
			@Override
			protected InputStream openURL(URL url) throws IOException {
				return ApplicationUpdateManagerImpl.this.openURL(url);
			}
        	
        });
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

	private boolean installPluginFile(String id, String version, Date date) {
        Path downloaded = downloadPlugin(id, version);

        Path pluginsRoot = pluginManager.getPluginsRoot();
        Path file = pluginsRoot.resolve(downloaded.getFileName());
        try {
            Files.move(downloaded, file, StandardCopyOption.REPLACE_EXISTING);
            Files.setLastModifiedTime(file, FileTime.fromMillis(date.getTime()));
            return true;
        } catch (IOException e) {
            throw new PluginRuntimeException(e, "Failed to write file '{}' to plugins folder", file);
        }
	}
	
    public Map<String, PluginInfo> getPluginsMap() {
        Map<String, PluginInfo> pluginsMap = super.getPluginsMap();
        if(pluginsMap.containsKey("jadaptive-boot")) {
        	pluginsMap.remove("jadaptive-boot");
        }
        return pluginsMap;
    }
    
    public PluginInfo getApplicationCoreUpdate() {
    	Map<String, PluginInfo> pluginsMap = super.getPluginsMap();
    	PluginInfo plugin = pluginsMap.get("jadaptive-boot");
    	
    	if(Objects.isNull(plugin)) {
    		return null;
    	}
    	if(plugin.releases.size() < 1) {
    		return null;
    	}
    	Version installedVersion = new Version(ApplicationVersion.getVersion());
    	PluginInfo.PluginRelease release = plugin.releases.get(0);
    	
    	log.debug("Checking core application version {} against {}", 
    			ApplicationVersion.getVersion(),
    			release.version);
    	
    	Version latestVersion = new Version(release.version);
    	if(!checkPluginVersion(installedVersion, latestVersion, release, plugin.id,
    		new File("app/jadaptive-boot-" + installedVersion.toString() + ".jar").toPath())) {
    		return null;
    	}

    	return plugin;
    }

	public boolean processApplicationPlugin(PluginInfo plugin) {
		
		PluginInfo.PluginRelease lastRelease = plugin.releases.get(0);
		
		log.debug("Install application core '{}' with version {}", plugin.id, lastRelease.version);
		
		try {
			File destination = new File("app");
			FileUtils.deleteDirectory(destination);
			destination.mkdirs();
			
			Path tmp = getFileDownloader(plugin.id).downloadFile(new URL(lastRelease.url));
            getFileVerifier(plugin.id).verify(new FileVerifier.Context(plugin.id, lastRelease), tmp);

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
	
	private InputStream openURL(URL url) throws IOException {
		if(url.getProtocol().startsWith("http")) {
			HttpURLConnection hc = (HttpURLConnection) url.openConnection();

	        hc.setDoOutput(false);
	        hc.setDoInput(true);
	        hc.setUseCaches(false);

	        hc.setRequestProperty("Product-Serial", ApplicationVersion.getSerial());
	        hc.setRequestProperty("Product-Id", ApplicationVersion.getProductId());
	        hc.setRequestProperty("Product-Version", ApplicationVersion.getVersion());
		       
	        hc.connect();
	        
	        return hc.getInputStream();

		} else {
			try {
				Path fromFile = Paths.get(url.toURI());
				return Files.newInputStream(fromFile);
			} catch (URISyntaxException e) {
	            throw new PluginRuntimeException("Something wrong with given URL", e);
	        }
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
	        String path = url.getPath();
	        String fileName = path.substring(path.lastIndexOf('/') + 1);
	        Path toFile = destination.resolve(fileName);

	        try(InputStream in = openURL(url)) {
		        Files.copy(in, toFile, StandardCopyOption.REPLACE_EXISTING);
		        return toFile;
	        }
		}
	}
	
	@Override
	@Scheduled(initialDelay = 60000, fixedDelay = 60000*60*24)
	public void check4Updates() {
		check4Updates(new ArrayList<>(), new ArrayList<>());
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
            hasUpdates = true;
        } else {
            log.debug("No updates found");
        }

        PluginInfo core = getApplicationCoreUpdate();
        if(core!=null) {
        	toUpdate.add(core);
        	hasUpdates = true;
        }
        
        if (hasAvailablePlugins()) {
            List<PluginInfo> availablePlugins = getAvailablePlugins();
            log.debug("Found {} available plugins", availablePlugins.size());
            for (PluginInfo plugin : availablePlugins) {
	            toInstall.add(plugin);
            }
            hasUpdates = true;
        } else {
            log.debug("No available plugins found");
        }

	}
	
	@Override
	public boolean installUpdates() throws IOException {

    	PluginInfo core = getApplicationCoreUpdate();
    	if(Objects.nonNull(core)) {
    		log.info("Updating core {}", core.id);
    		processApplicationPlugin(core);
    	}
    	
        for (PluginInfo plugin : getUpdates()) {
            log.debug("Found update for plugin '{}'", plugin.id);
            PluginInfo.PluginRelease lastRelease = getLastPluginRelease(plugin.id);
            Version updateVersion = new Version(lastRelease.version);
            
            PluginWrapper wrapper = pluginManager.getPlugin(plugin.id);
            Version installedVersion = new Version(wrapper.getDescriptor().getVersion());
            log.debug("Update plugin '{}' from version {} to version {}", plugin.id, installedVersion, updateVersion);

            // Only delete plug-in if its a different version
            if(installedVersion.compareTo(updateVersion) != 0) {
            	if(!pluginManager.deletePlugin(plugin.id)) {
            		throw new IOException("Unable to delete plugin " + plugin.id);
            	}
            }
            
            if(!installPluginFile(plugin.id, lastRelease.version, lastRelease.date)) {
            	throw new IOException("Could not update plugin " + plugin.id);
            }
        }
	
        for (PluginInfo plugin : getAvailablePlugins()) {
        	
        	PluginInfo.PluginRelease lastRelease = getLastPluginRelease(plugin.id);
        	
        	log.debug("Installing plugin '{} from {}'", plugin.id, plugin.projectUrl);
        	
        	if(!installPluginFile(plugin.id, lastRelease.version, lastRelease.date)) {
            	throw new IOException("Could not install plugin " + plugin.id);
            }
        }

        return true;
	}

	@Override
	public boolean hasPendingUpdates() {
		return hasUpdates;
	}
	
	@Override
	public void restart() {
		Application.restart();
	}
	
	@Override
	public void shutdown() {
		Application.shutdown();
	}
}

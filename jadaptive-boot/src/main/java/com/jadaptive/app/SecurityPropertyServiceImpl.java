package com.jadaptive.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ConfigHelper;
import com.jadaptive.api.app.ResourcePackage;
import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.app.SecurityScope;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.utils.FileUtils;

@Service
public class SecurityPropertyServiceImpl implements SecurityPropertyService {

	static Logger log = LoggerFactory.getLogger(SecurityPropertyService.class);
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PluginManager pluginManager;
	
	@Override
	public Properties resolveSecurityProperties(String resourceUri) throws FileNotFoundException {
		return resolveSecurityProperties(resourceUri, false);
	}
	
	@Override
	public Properties resolveSecurityProperties(String resourceUri, boolean uriOnly) throws FileNotFoundException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		
		resourceUri = resourceUri.replaceFirst("/app/", "");
		
		List<Path> securityProperties = new ArrayList<>();
		
		
		try {
			addClasspathResources("/webapp" + FileUtils.checkStartsWithSlash(resourceUri), securityProperties, uriOnly);
		} catch (IOException e) {
			log.error("Failed to read security properties from system classpath", e);
		}
		
		if(!tenant.isSystem()) {
			
		    try {
				securityProperties.addAll(resolveSecurityFiles(resourceUri, 
						ConfigHelper.getTenantSubFolder(tenant, "webapp"),
						ConfigHelper.getTenantPackages(tenant), uriOnly));
			} catch (IOException e) {
				log.error("Failed to read security properties of tenant packages", e);
			}
			
		} else {
			
			try {
				securityProperties.addAll(resolveSecurityFiles(resourceUri, 
						ConfigHelper.getSystemPrivateSubFolder("webapp"),
						ConfigHelper.getSystemPrivatePackages(), uriOnly));
			} catch (IOException e) {
				log.error("Failed to read security properties of system packages", e);
			}
		}
		
		try {
			securityProperties.addAll(resolveSecurityFiles(resourceUri, 
					ConfigHelper.getSharedSubFolder("webapp"),
					ConfigHelper.getSharedPackages(), uriOnly));
		} catch (IOException e) {
			log.error("Failed to read security properties of shared packages", e);
		}
		

		
		Properties properties = new Properties();
		Collections.reverse(securityProperties);
		for(Path path : securityProperties) {
			try {
				properties.putAll(readPropertes(path));
			} catch (IOException e) {
				log.error("Failed to read properties from path {}", path.toString(), e);
			}
		}
		
		if(log.isInfoEnabled()) {
			for(String name : properties.stringPropertyNames()) {
				log.info("{} = {}", name, properties.get(name));
			}
		}
		
		return properties;
	}
	
	private void addClasspathResources(String path, List<Path> paths, boolean uriOnly) throws IOException {
		
		List<String> parentFolders = new ArrayList<>();
		if(path.endsWith("/")) {
			parentFolders.add(path);
		}
		if(!uriOnly) {
			parentFolders.addAll(FileUtils.getParentPaths(FileUtils.checkStartsWithSlash(path)));
		}
		for(String parentFolder : parentFolders) {
			String securityFile = FileUtils.checkEndsWithSlash(parentFolder) + "security.properties";
			for(PluginWrapper w : pluginManager.getPlugins()) {
				PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(w.getPluginClassLoader());
				Resource[] resources = resolver.getResources("classpath*:" + securityFile);
				for(Resource resource : resources) {
					try {
						paths.add(Paths.get(resource.getURL().toURI()));
					} catch (URISyntaxException e) {
					}
				}
			}
			
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
			Resource[] resources = resolver.getResources("classpath*:" + securityFile);
			for(Resource resource : resources) {
				try {
					paths.add(Paths.get(resource.getURL().toURI()));
				} catch (URISyntaxException e) {
				}
			}
		}
		
	
	}

	private Properties readPropertes(Path path) throws IOException {
		
		Properties properties = new Properties();
		try(InputStream in = Files.newInputStream(path)) {
			properties.load(in);
			return properties;
		}
	}
	
	private List<Path> resolveSecurityFiles(String resourceUri, File rootFolder, Collection<ResourcePackage> packages, boolean uriOnly) {
		List<Path> securityProperties = new ArrayList<>();
		List<String> parentFolders = new ArrayList<>();
		if(resourceUri.endsWith("/")) {
			parentFolders.add(resourceUri);
		}
		
		if(!uriOnly) {
			parentFolders.addAll(FileUtils.getParentPaths(FileUtils.checkStartsWithSlash(resourceUri)));
		}
		
		for(String parentFolder : parentFolders) {
			String securityFile = FileUtils.checkEndsWithSlash(parentFolder) + "security.properties";
			File res = new File(rootFolder, securityFile);
			if(res.exists()) {
				securityProperties.add(res.toPath());
			}
			String uri = FileUtils.checkEndsWithSlash("webapp") + securityFile;
			for(ResourcePackage pkg : packages) {
				if(pkg.containsPath(uri)) {
					securityProperties.add(pkg.resolvePath(uri));
				}
			}
		}
		return securityProperties;
	}

	@Override
	public Properties getOverrideProperties(SecurityScope scope, String resourceUri) throws IOException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		resourceUri = resourceUri.replaceFirst("/app/", "");
		
		switch(scope) {
		case PRIVATE:
			return getPropertiesForPath(ConfigHelper.getSystemPrivateSubFolder("webapp"), resourceUri);
		case TENANT:
			return getPropertiesForPath(ConfigHelper.getTenantSubFolder(tenant, "webapp"), resourceUri);
		default:
			return getPropertiesForPath(ConfigHelper.getSharedSubFolder("webapp"), resourceUri);
	}
}
	
	private Properties getPropertiesForPath(File path, String resourceUri) throws IOException {
		File parentFolder = new File(path, resourceUri);
		File res = new File(parentFolder, "security.properties");
	
		Properties properties = new Properties();
		if(res.exists()) {
			try(InputStream in = new FileInputStream(res)) {
				properties.load(in);
			}
		}
		return properties;
	}
	
	private void savePropertiesForPath(File path, String resourceUri, Properties newProperties) throws IOException {
		
		File parentFolder = new File(path, resourceUri);
		File res = new File(parentFolder, "security.properties");
	
		Properties properties = new Properties();
		if(res.exists()) {
			try(InputStream in = new FileInputStream(res)) {
				properties.load(in);
			}
		}
		properties.putAll(newProperties);
		
		if(!res.exists()) {
			res.getParentFile().mkdirs();
			res.createNewFile();
		}
		
		try(OutputStream out = new FileOutputStream(res)) {
			properties.store(out, "");
		}
	}

	@Override
	public void saveProperty(SecurityScope scope, String resourceUri, String key, String value) throws IOException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		Properties properties = new Properties();
		properties.put(key, value);
		
		switch(scope) {
		case PRIVATE:
			savePropertiesForPath(ConfigHelper.getSystemPrivateSubFolder("webapp"), resourceUri, properties);
			break;
		case TENANT:
			savePropertiesForPath(ConfigHelper.getTenantSubFolder(tenant, "webapp"), resourceUri, properties);
			break;
		default:
			savePropertiesForPath(ConfigHelper.getSharedSubFolder("webapp"), resourceUri, properties);
			break;
		}
	}

	@Override
	public void deleteProperty(SecurityScope scope, String resourceUri, String key) throws IOException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		File parentFolder;
		
		switch(scope) {
		case PRIVATE:
			parentFolder = new File(ConfigHelper.getSystemPrivateSubFolder("webapp"), resourceUri);
			break;
		case TENANT:
			parentFolder = new File(ConfigHelper.getTenantSubFolder(tenant, "webapp"), resourceUri);
			break;
		default:
			parentFolder = new File(ConfigHelper.getSharedSubFolder("webapp"), resourceUri);
			break;
		}
		
		File res = new File(parentFolder, "security.properties");
	
		Properties properties = new Properties();
		if(res.exists()) {
			try(InputStream in = new FileInputStream(res)) {
				properties.load(in);
			}
		}
		properties.remove(key);
		
		try(OutputStream out = new FileOutputStream(res)) {
			properties.store(out, "");
		}
		
	}
}

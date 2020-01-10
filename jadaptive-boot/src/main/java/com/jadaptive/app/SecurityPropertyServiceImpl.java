package com.jadaptive.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

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
	public Properties resolveSecurityProperties(HttpServletRequest request, String resourceUri) throws FileNotFoundException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		
		resourceUri = resourceUri.replaceFirst("/app/", "");
		
		List<Path> securityProperties = new ArrayList<>();
		
		
		try {
			addClasspathResources("/webapp" + FileUtils.checkStartsWithSlash(resourceUri), securityProperties);
		} catch (IOException e) {
			log.error("Failed to read security properties from system classpath", e);
		}
		
		if(!tenant.getSystem()) {
			
		    try {
				securityProperties.addAll(resolveSecurityFiles(resourceUri, 
						ConfigHelper.getTenantSubFolder(tenant, "webapp"),
						ConfigHelper.getTenantPackages(tenant)));
			} catch (IOException e) {
				log.error("Failed to read security properties of tenant packages", e);
			}
			
		} else {
			
			try {
				securityProperties.addAll(resolveSecurityFiles(resourceUri, 
						ConfigHelper.getSystemPrivateSubFolder("webapp"),
						ConfigHelper.getSystemPrivatePackages()));
			} catch (IOException e) {
				log.error("Failed to read security properties of system packages", e);
			}
		}
		
		try {
			securityProperties.addAll(resolveSecurityFiles(resourceUri, 
					ConfigHelper.getSharedSubFolder("webapp"),
					ConfigHelper.getSharedPackages()));
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
	
	private void addClasspathResources(String path, List<Path> paths) throws IOException {
		
		List<String> parentFolders = new ArrayList<>();
		if(path.endsWith("/")) {
			parentFolders.add(path);
		}
		
		parentFolders.addAll(FileUtils.getParentPaths(FileUtils.checkStartsWithSlash(path)));
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
		properties.load(Files.newInputStream(path));
		return properties;
	}
	
	private List<Path> resolveSecurityFiles(String resourceUri, File rootFolder, Collection<ResourcePackage> packages) {
		List<Path> securityProperties = new ArrayList<>();
		List<String> parentFolders = new ArrayList<>();
		if(resourceUri.endsWith("/")) {
			parentFolders.add(resourceUri);
		}
		parentFolders.addAll(FileUtils.getParentPaths(FileUtils.checkStartsWithSlash(resourceUri)));
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
}

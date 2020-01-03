package com.jadaptive.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.utils.FileUtils;

@Service
public class SecurityPropertyServiceImpl implements SecurityPropertyService {

	static Logger log = LoggerFactory.getLogger(SecurityPropertyService.class);
	
	@Autowired
	TenantService tenantService; 
	
	@Override
	public Properties resolveSecurityProperties(HttpServletRequest request, String resourceUri) throws FileNotFoundException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		
		resourceUri = resourceUri.replaceFirst("/app/", "");
		
		List<Path> securityProperties = new ArrayList<>();
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

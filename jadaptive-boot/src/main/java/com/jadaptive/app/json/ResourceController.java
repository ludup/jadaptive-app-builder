package com.jadaptive.app.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jadaptive.api.app.ConfigHelper;
import com.jadaptive.api.app.ResourcePackage;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.utils.FileUtils;

@Controller
public class ResourceController {
	
	static Logger log = LoggerFactory.getLogger(ResourceController.class);
	
	@Autowired
	TenantService tenantService; 
	
	@Autowired
	PluginManager pluginManager; 
	
	@PostConstruct
	private void postConstruct() {
		System.out.println(getClass().getName());
	}
	
	@RequestMapping(value="", method = RequestMethod.GET)
	public void doDefaultPath(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, EntityException, IOException {
		ResponseHelper.sendRedirect("/app/", request, response);
	}
	
	@RequestMapping(value="app/**", method = RequestMethod.GET)
	public void doResourceGet(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, EntityException, IOException {

		tenantService.setCurrentTenant(request);
		
		String uri = request.getRequestURI();
		
		try {
			
			String resourceUri = uri.length() >= 4 ? uri.substring(4) : "";
			
			if(resourceUri.endsWith("security.properties")) {
				ResponseHelper.send404NotFound(uri, request, response);
				return;
			}
			
			Path resource = resolveResource(request, resourceUri);
			
			if(Files.exists(resource) && Files.isDirectory(resource) && !uri.endsWith("/")) {
				ResponseHelper.sendRedirect(uri + "/", request, response);
				return;
			}
			
			if(!Files.exists(resource)  || Files.isDirectory(resource) ) {
				if(!Files.isDirectory(resource)) {
					resource = resolveResource(request, FileUtils.checkEndsWithNoSlash(resourceUri) + ".html");
				} else {
					resource = resolveResource(request, FileUtils.checkEndsWithSlash(resourceUri) + "index.html");
				}
				
				if(!Files.exists(resource)) {
					ResponseHelper.send404NotFound(uri, request, response);
					return;
				}
			} 
			
			ResponseHelper.sendContent(resource, uri, request, response);
		
		} catch(FileNotFoundException e) { 
			ResponseHelper.send404NotFound(uri, request, response);
		} finally {
			tenantService.clearCurrentTenant();
		}

	}

	private Path resolveResource(HttpServletRequest request, String resourceUri) throws FileNotFoundException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		
		String uri = "webapp" + resourceUri;
		
		if(!tenant.getSystem()) {
			
			/**
			 * Process tenant files, then packages
			 */
			File res = new File(ConfigHelper.getTenantSubFolder(tenant, "webapp"), resourceUri);
			if(res.exists()) {
				return res.toPath();
			}
			
			try {
				for(ResourcePackage pkg : ConfigHelper.getTenantPackages(tenant)) {
					if(pkg.containsPath(uri)) {
						return pkg.resolvePath(uri);
					}
				}
			} catch (IOException e) {
				log.error("Failed to process package file for " + uri, e);
			}
			
		} else {
			
			/**
			 * Process system files, then packages
			 */
			File res = new File(ConfigHelper.getSystemPrivateSubFolder("webapp"), resourceUri);
			if(res.exists()) {
				return res.toPath();
			}			
			
			try {
				for(ResourcePackage pkg : ConfigHelper.getSystemPrivatePackages()) {
					if(pkg.containsPath(uri)) {
						return pkg.resolvePath(uri);
					}
				}
			} catch (IOException e) {
				log.error("Failed to process package file for " + uri, e);
			}
		}
		
		File res = new File(ConfigHelper.getSharedSubFolder("webapp"), resourceUri);
		if(res.exists()) {
			return res.toPath();
		}
		
		try {
			for(ResourcePackage pkg : ConfigHelper.getSharedPackages()) {
				if(pkg.containsPath(uri)) {
					return pkg.resolvePath(uri);
				}
			}
		} catch (IOException e) {
			log.error("Failed to process package file for " + uri, e);
		}
		
		for(PluginWrapper w : pluginManager.getPlugins()) {
			URL url = w.getPluginClassLoader().getResource(FileUtils.checkStartsWithNoSlash(uri));
			if(Objects.nonNull(url)) {
				try {
					return Paths.get(url.toURI());
				} catch (URISyntaxException e) {
					log.error("Failed to process classpath resource for " + uri, e);
				}
			}
		}
		
		URL url = getClass().getClassLoader().getResource(resourceUri);
		if(Objects.nonNull(url)) {
			try {
				return Paths.get(url.toURI());
			} catch (URISyntaxException e) {
				log.error("Failed to process classpath resource for " + uri, e);
			}
		}
		
		return res.toPath();

	}


	

}

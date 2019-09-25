package com.jadaptive.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jadaptive.app.ConfigHelper;
import com.jadaptive.app.ZipPackage;
import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantService;
import com.jadaptive.utils.FileUtils;

@Controller
public class ResourceController {
	
	static Logger log = LoggerFactory.getLogger(ResourceController.class);
	
	@Autowired
	TenantService tenantService; 
	
	@RequestMapping(value="", method = RequestMethod.GET)
	public void doDefaultPath(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, EntityException, IOException {
		sendRedirect("/app/", request, response);
	}
	
	@RequestMapping(value="app/**", method = RequestMethod.GET)
	public void doResourceGet(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, EntityException, IOException {

		tenantService.setCurrentTenant(request);
		
		String uri = request.getRequestURI();
		
		try {
			
			String resourceUri = uri.length() >= 4 ? uri.substring(4) : "";
			
			Path resource = resolveResource(request, resourceUri);
			
			if(Files.exists(resource) && Files.exists(resource)  && !uri.endsWith("/")) {
				sendRedirect(uri + "/", request, response);
				return;
			}
			
			if(!Files.exists(resource)  || Files.isDirectory(resource) ) {
				if(!Files.isDirectory(resource)) {
					resource = resolveResource(request, FileUtils.checkEndsWithNoSlash(resourceUri) + ".html");
				} else {
					resource = resolveResource(request, FileUtils.checkEndsWithSlash(resourceUri) + "index.html");
				}
				
				if(!Files.exists(resource)) {
					send404NotFound(uri, request, response);
					return;
				}
			} 
			
			sendContent(resource, uri, request, response);
		
		} catch(FileNotFoundException e) { 
			send404NotFound(uri, request, response);
		} finally {
			tenantService.clearCurrentTenant();
		}

	}

	private Path resolveResource(HttpServletRequest request, String resourceUri) throws FileNotFoundException {
		
		Tenant tenant = tenantService.getCurrentTenant();
		
		if(!tenant.getSystem()) {
			File res = new File(ConfigHelper.getTenantSubFolder(tenant, "webapp"), resourceUri);
			if(res.exists()) {
				return res.toPath();
			}
		} else {
			File res = new File(ConfigHelper.getSystemPrivateSubFolder("webapp"), resourceUri);
			if(res.exists()) {
				return res.toPath();
			}
			
			String uri = "webapp/" + resourceUri;
			try {
				for(ZipPackage pkg : ConfigHelper.getSystemPrivatePackages()) {
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
		
		String uri = "webapp" + resourceUri;
		try {
			for(ZipPackage pkg : ConfigHelper.getSharedPackages()) {
				if(pkg.containsPath(uri)) {
					return pkg.resolvePath(uri);
				}
			}
		} catch (IOException e) {
			log.error("Failed to process package file for " + uri, e);
		}
		
		return res.toPath();

	}

	private void sendRedirect(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(uri);
	}

	private void sendContent(Path resource, String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException {
		
		response.setStatus(HttpStatus.OK.value());
		BasicFileAttributes attr = Files.readAttributes(resource, BasicFileAttributes.class);
		response.setContentLengthLong(attr.size());
		try(InputStream in = Files.newInputStream(resource)) {
			IOUtils.copy(in, response.getOutputStream());
		}
	}

	private void send404NotFound(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// TODO 404 error template
		
		response.sendError(HttpStatus.NOT_FOUND.value());
	}
	

}

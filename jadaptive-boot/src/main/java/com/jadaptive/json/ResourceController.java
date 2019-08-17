package com.jadaptive.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantService;

@Controller
public class ResourceController {
	
	@Autowired
	TenantService tenantService; 
	
	@RequestMapping(value="", method = RequestMethod.GET)
	public void doDefaultPath(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, EntityException, IOException {
		sendRedirect("/app/", request, response);
	}
	
	@RequestMapping(value="app/**", method = RequestMethod.GET)
	public void doResourceGet(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, EntityException, IOException {

		tenantService.setCurrentTenant(request);
		
		try {
			String uri = request.getRequestURI();
			String resourceUri = uri.length() >= 4 ? uri.substring(4) : "";
			
			File resource = resolveResource(request, resourceUri);
			
			if(resource.exists() && resource.isDirectory() && !uri.endsWith("/")) {
				sendRedirect(uri + "/", request, response);
				return;
			}
			
			if(!resource.exists() || resource.isDirectory()) {
				resource = new File(resource.getParentFile(), 
						resource.isDirectory() ? resource.getName() + File.separator + "index.html" : resource.getName() + ".html");
				if(!resource.exists()) {
					send404NotFound(uri, request, response);
					return;
				}
			} 
			
			sendContent(resource, uri, request, response);
		
		} finally {
			tenantService.clearCurrentTenant();
		}

	}

	private File resolveResource(HttpServletRequest request, String resourceUri) {
		
		Tenant tenant = tenantService.getCurrentTenant();
		
		if(!tenant.getSystem()) {
			File webapp = new File("conf" + File.separator 
					+ "tenants" + File.separator 
					+ tenant.getHostname() + File.separator 
					+ "webapp", resourceUri);
			if(webapp.exists()) {
				return webapp;
			}
		}
		
		return new File("conf" + File.separator 
				+ "system" + File.separator 
				+ "webapp", resourceUri);
	}

	private void sendRedirect(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(uri);
	}

	private void sendContent(File resource, String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException {
		
		response.setStatus(HttpStatus.OK.value());
		response.setContentLengthLong(resource.length());
		try(InputStream in = new FileInputStream(resource)) {
			IOUtils.copy(in, response.getOutputStream());
		}
	}

	private void send404NotFound(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// TODO 404 error template
		
		response.sendError(HttpStatus.NOT_FOUND.value());
	}
	

}

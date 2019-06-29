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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.RepositoryException;

@Controller
public class ResourceController {
	
	@RequestMapping(value="app/**", method = RequestMethod.GET)
	public void doResourceGet(HttpServletRequest request, HttpServletResponse response) throws RepositoryException, UnknownEntityException, EntityNotFoundException, IOException {

		String uri = request.getRequestURI();
		String resourceUri = uri.length() >= 4 ? uri.substring(4) : "";
		
		File webapp = new File(System.getProperty("jadaptive.webapp.path"), "webapp");
		File resource = new File(webapp, resourceUri);
		if(resource.exists() && resource.isDirectory() && !uri.endsWith("/")) {
			sendRedirect(uri + "/", request, response);
			return;
		}
		
		if(!resource.exists() || resource.isDirectory()) {
			resource = new File(webapp, resource.isDirectory() ? resourceUri + "index.html" : resourceUri + ".html");
			if(!resource.exists()) {
				send404NotFound(uri, request, response);
				return;
			}
		} 
		
		sendContent(resource, uri, request, response);

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

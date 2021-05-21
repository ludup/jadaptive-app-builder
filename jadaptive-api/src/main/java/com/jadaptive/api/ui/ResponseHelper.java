package com.jadaptive.api.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class ResponseHelper {

	static Logger log = LoggerFactory.getLogger(ResponseHelper.class);
	
	public static void sendRedirect(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(uri);
	}

	public static  void sendContent(Path resource, String contentType, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException {
		
		if(log.isDebugEnabled()) {
			log.debug("Returning resource {} with type {}", resource.toString(), contentType);
		}
		response.setStatus(HttpStatus.OK.value());
		BasicFileAttributes attr = Files.readAttributes(resource, BasicFileAttributes.class);
		response.setContentLengthLong(attr.size());
		response.setContentType(contentType);

		try(InputStream in = Files.newInputStream(resource)) {
			IOUtils.copy(in, response.getOutputStream());
		}
	}
	
	public static  void sendContent(String content, String contentType, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException {
		
		if(log.isDebugEnabled()) {
			log.debug("Returning resource {} with type {}", request.getRequestURI(), contentType);
		}
		
		response.setStatus(HttpStatus.OK.value());
		byte[] buf = content.getBytes("UTF-8");
		
		response.setContentType(contentType);
		response.setContentLengthLong(buf.length);
		
		response.getOutputStream().write(buf);
		response.getOutputStream().flush();
	}

	public static  void send404NotFound(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// TODO 404 error template
		
		response.sendError(HttpStatus.NOT_FOUND.value());
	}

	public static void send403Forbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// TODO 403 error template
		
		response.sendError(HttpStatus.FORBIDDEN.value());
		
	}
}

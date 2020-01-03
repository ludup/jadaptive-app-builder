package com.jadaptive.app.json;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;

public class ResponseHelper {

	public static void sendRedirect(String uri, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(uri);
	}

	public static  void sendContent(Path resource, String uri, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException {
		
		response.setStatus(HttpStatus.OK.value());
		BasicFileAttributes attr = Files.readAttributes(resource, BasicFileAttributes.class);
		response.setContentLengthLong(attr.size());
		try(InputStream in = Files.newInputStream(resource)) {
			IOUtils.copy(in, response.getOutputStream());
		}
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

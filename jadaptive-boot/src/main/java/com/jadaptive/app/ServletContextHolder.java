package com.jadaptive.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletContext;

@Component
public class ServletContextHolder {
	
	@Autowired
	ServletContext servletContext;
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	public ServletContext getServletContext() {
		return servletContext;
	}
}

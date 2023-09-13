package com.jadaptive.api.ui;

public interface PageResources {

	String getHtmlResource();
	
	String getCssResource();
	
	String getJsResource();
	
	default Class<?> getResourceClass() { return getClass(); };
}

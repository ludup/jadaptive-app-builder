package com.jadaptive.api.product;

import org.pf4j.ExtensionPoint;

public interface Product extends ExtensionPoint {

	String getName();
	
	String getVersion();
	
	default String getLogoResource() {
		return "/app/content/images/jadaptive-logo.png";
	}

	default String getFaviconResource() {
		return "/app/content/images/jadaptive-favicon.png";
	}
}

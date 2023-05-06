package com.jadaptive.api.product;

import org.pf4j.ExtensionPoint;

public interface Product extends ExtensionPoint {

	default String getName() {
		return "Jadaptive App Builder";
	}
	
	default String getVersion() {
		return "DEV_VERSION";
	}
	
	default String getPoweredBy() {
		return "Powered by Jadaptive";
	}
	
	default String getLogoResource() {
		return "/app/content/images/jadaptive-logo.png";
	}

	default String getFaviconResource() {
		return "/app/content/images/jadaptive-favicon.png";
	}
}

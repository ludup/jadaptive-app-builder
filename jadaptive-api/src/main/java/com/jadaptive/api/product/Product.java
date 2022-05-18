package com.jadaptive.api.product;

import org.pf4j.ExtensionPoint;

public interface Product extends ExtensionPoint {

	String getName();
	
	String getVersion();
	
	String getLogoResource();
	
	String getFaviconResource();
}

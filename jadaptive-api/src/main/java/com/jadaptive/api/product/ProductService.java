package com.jadaptive.api.product;

public interface ProductService {

	boolean supportsFeature(String feature);

	String getVersion();
	
	String getCopyright();

}

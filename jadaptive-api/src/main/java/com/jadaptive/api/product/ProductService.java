package com.jadaptive.api.product;

public interface ProductService {

	String getVersion();
	
	String getCopyright();

	String getLogoResource();

	String getFaviconResource();

	String getProductName();

	String getPoweredBy();

	String getProductCode();

	String getVendor();

	boolean requiresRegistration();

	boolean isTenantLicensing();

}

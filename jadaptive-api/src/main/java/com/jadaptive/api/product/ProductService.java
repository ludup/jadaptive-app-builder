package com.jadaptive.api.product;

public interface ProductService {
	
	public enum ImageURIFormat {
		PUBLIC_WEB_URI, BASE64_ENCODED
	}

	public enum ProductId {
		FRAMEWORK,
		PASSWORD_EXPRESS_ONPREM,
		PASSWORD_EXPRESS_CLOUD,
		NODAL_ONPREM,
		SSH_PROXY_CLOUD,
		SSH_PROXY_ONPREM,
		LICENSE_SERVER,
		GABBLE_CLOUD,
		SSH_TEAM_CLOUD,
		SUPPORT_CALLBACK,
		NODAL_CLOUD, 
		CHANGELOG_ARCHITECT
	}
	
	ProductId getProductId(); 
	
	String getVersion();
	
	String getCopyright();

	String getLogoResource(ImageURIFormat imageFormat);

	String getFaviconResource(ImageURIFormat imageFormat);

	String getProductName();

	String getPoweredBy();

	String getProductCode();

	String getVendor();

	boolean requiresRegistration();

	boolean isTenantLicensing();

	boolean isRevenueGenerating();

	boolean supportsPAYG();

}

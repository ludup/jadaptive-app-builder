package com.jadaptive.api.product;

public interface ProductService {
	
	public enum ImageURIFormat {
		PUBLIC_WEB_URI, BASE64_ENCODED
	}

	public enum ProductId {
		FRAMEWORK,
		SECURE_FILE_EXCHANGE_CLOUD,
		SECURE_FILE_EXCHANGE_ONPREM,
		DEBIAN_REPOSITORY,
		SECURE_FILE_EXCHANGE_EC2,
		PASSWORD_EXPRESS_ONPREM,
		JAD_VPN_ONPREM,
		LOGONBOX_VPN_EC2, 
		SSH_PROXY_CLOUD,
		SSH_PROXY_ONPREM,
		SSH_PROXY_EC2,
		VMSEE_CLOUD,
		VMSEE_ONPREM,
		VMSEE_EC2,
		SECURE_NODE,
		LICENSE_SERVER,
		WINDOWS_CONNECT,
		GABBLE_CLOUD,
		PASSWORD_EXPRESS_CLOUD,
		NODAL_CLOUD
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

package com.jadaptive.api.product;

public interface ProductService {

	public enum ProductId {
		FRAMEWORK,
		SECURE_FILE_EXCHANGE_CLOUD,
		SECURE_FILE_EXCHANGE_OMPREM,
		DEBIAN_REPOSITORY,
		SECURE_FILE_EXCHANGE_EC2,
		LOGONBOX_EXPRESS_CLOUD,
		LOGONBOX_EXPRESS_ONPREM,
		LOGONBOX_EXPRESS_EC2,
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
		WINDOWS_CONNECT
	}
	
	ProductId getProductId(); 
	
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

	boolean isRevenueGenerating();

}

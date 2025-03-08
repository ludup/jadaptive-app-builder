package com.jadaptive.api.auth.oauth2;

public enum GrantType {
	AUTHORIZATION_CODE, PASSWORD, CLIENT_CREDENTIALS, REFRESH_TOKEN, DEVICE;
	
	public static final String URN_IETF_PARAMS_OAUTH_GRANT_TYPE_DEVICE_CODE = "urn:ietf:params:oauth:grant-type:device_code";

	public final static GrantType fromGrantType(String grantType) {
		if(grantType.equals(URN_IETF_PARAMS_OAUTH_GRANT_TYPE_DEVICE_CODE)) {
			return GrantType.DEVICE;
		}
		else 
			return GrantType.valueOf(grantType.toUpperCase());
	}
	
	public String toGrantType() {
		switch(this) {
		case DEVICE:
			return URN_IETF_PARAMS_OAUTH_GRANT_TYPE_DEVICE_CODE;
		default:
			return name().toLowerCase();
		}
	}
}

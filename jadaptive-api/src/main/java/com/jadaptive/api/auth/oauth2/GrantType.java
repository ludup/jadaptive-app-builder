package com.jadaptive.api.auth.oauth2;

public enum GrantType {
	AUTHORIZATION_CODE, PASSWORD, CLIENT_CREDENTIALS, REFRESH_TOKEN, DEVICE;
	
	public final static GrantType fromGrantType(String grantType) {
		if(grantType.equals("urn:ietf:params:oauth:grant-type:device_code")) {
			return GrantType.DEVICE;
		}
		else 
			return GrantType.valueOf(grantType.toUpperCase());
	}
}

package com.jadaptive.api.auth;

public enum AuthenticationScope {

	USER_LOGIN(UserLoginAuthenticationPolicy.RESOURCE_KEY),
	PASSWORD_RESET(PasswordResetAuthenticationPolicy.RESOURCE_KEY),
    SAML_IDP(SAMLIdpAuthenticationPolicy.RESOURCE_KEY);
    
	String resourceKey;
	private AuthenticationScope(String resourceKey) {
		this.resourceKey = resourceKey;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}
}

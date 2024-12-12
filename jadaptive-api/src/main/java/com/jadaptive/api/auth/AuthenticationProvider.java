package com.jadaptive.api.auth;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.user.User;

public interface AuthenticationProvider extends ExtensionPoint {

	public boolean hasSufficientCredentials(User user);
	
	public boolean supportsMultipleCredentials();
	
	public default boolean supportsCredentialReset() { return false; }
	
	public default String getEnrollmentUri() { return "/app/api/start-temporary-auth"; }
	
	public String getManagementUri();
	
	public String getAuthenticatorUUID();
	
	public String getAuthenticatorKey();
	
	public boolean isSecretCapture();
	
	public boolean isIdentityCapture();

	public String getName();
	
	public default void resetCredentials() { throw new UnsupportedOperationException(); }
}

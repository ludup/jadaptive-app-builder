package com.jadaptive.api.auth;

import org.pf4j.ExtensionPoint;

public interface AuthenticationStep extends ExtensionPoint {

	String getResourceKey();
	
	boolean isIdentityStep();
	
	boolean isSecretStep();

	String getConfigurationPage();

	String getBundle();

	String getIcon();
	
	boolean isReady();

	default String getIconGroup() { return "fa-solid"; };
}

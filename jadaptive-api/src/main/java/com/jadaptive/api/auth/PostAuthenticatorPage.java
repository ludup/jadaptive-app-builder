package com.jadaptive.api.auth;

import com.jadaptive.api.ui.Page;

public interface PostAuthenticatorPage extends Page {

	boolean requiresProcessing(AuthenticationState state);
	
	default Integer getWeight() { return Integer.MIN_VALUE; };
}

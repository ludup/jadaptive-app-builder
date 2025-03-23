package com.jadaptive.api.auth;

import com.jadaptive.api.ui.Page;
import com.jadaptive.api.user.User;

public interface PostAuthenticatorPage extends Page {

	boolean requiresProcessing(AuthenticationPolicy policy, User user);
	
	default Integer getWeight() { return Integer.MIN_VALUE; };
}

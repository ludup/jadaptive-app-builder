package com.jadaptive.api.auth;

import org.jsoup.nodes.Document;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

public interface AuthenticationService {

	public static final String USER_LOGIN_PERMISSION =  "users.login";
	public static final String PASSWORD = "password";
	public static final String ALTERNATIVE_PASSWORD = "alternativePassword";
	
	Session logonUser(String username, String password, Tenant tenant, String remoteAddress, String userAgent);

	AuthenticationState getCurrentState();

	Class<?> completeAuthentication(User user);

	void registerAuthenticationPage(String resourceKey, Class<?> page);

	Class<?> resetAuthentication(Class<?>... additionalPages);

	Class<?> resetAuthentication(String authenticationFlow, Class<?>... additionalPages);

	void clearAuthenticationState();

	void decorateAuthenticationPage(Document content);

	void reportAuthenticationFailure(String username);


	
}

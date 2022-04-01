package com.jadaptive.api.auth;

import java.io.FileNotFoundException;

import org.jsoup.nodes.Document;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.ui.Page;

public interface AuthenticationService {

	public static final String USER_LOGIN_PERMISSION =  "users.login";
	public static final String ALTERNATIVE_PASSWORD = "alternativePassword";
	public static final String AUTHENTICATION_STATE_ATTR = "authenticationState";
	public static final String DEFAULT_AUTHENTICATION_FLOW = "defaultAuthentication";
	public static final String PASSWORD = "password";
	
	Session logonUser(String username, String password, Tenant tenant, String remoteAddress, String userAgent);

	AuthenticationState getCurrentState() throws FileNotFoundException;

	Class<? extends Page> completeAuthentication(AuthenticationState state);

	void registerAuthenticationPage(String resourceKey, Class<? extends Page> page);

	Class<? extends Page> resetAuthentication(@SuppressWarnings("unchecked") Class<? extends Page>... additionalPages);

	Class<? extends Page> resetAuthentication(String authenticationFlow, @SuppressWarnings("unchecked") Class<? extends Page>... additionalPages);

	void clearAuthenticationState();

	void decorateAuthenticationPage(Document content);

	void reportAuthenticationFailure(AuthenticationState state);

	Class<? extends Page> getAuthenticationPage(String authenticator);

	void processRequiredAuthentication(AuthenticationState state, AuthenticationPolicy policy) throws FileNotFoundException;


	
}

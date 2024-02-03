package com.jadaptive.api.auth;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.jsoup.nodes.Document;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.user.User;

public interface AuthenticationService {

	public static final String USER_LOGIN_PERMISSION =  "users.login";
	public static final String ALTERNATIVE_PASSWORD = "alternativePassword";
	public static final String AUTHENTICATION_STATE_ATTR = "authenticationState";
	public static final String DEFAULT_AUTHENTICATION_FLOW = "defaultAuthentication";
	public static final String PASSWORD = "password";
	
	public static final String PASSWORD_MODULE_UUID = "b76a4b67-ac70-45c2-95ca-9d7e14b3f695";
	
	Session logonUser(String username, String password, Tenant tenant, String remoteAddress, String userAgent);

	AuthenticationState getCurrentState() throws FileNotFoundException;

	Class<? extends Page> completeAuthentication(AuthenticationState state, Optional<Page> page);

	Class<? extends Page> resetAuthentication(@SuppressWarnings("unchecked") Class<? extends Page>... additionalPages);

	Class<? extends Page> resetAuthentication(String authenticationFlow, @SuppressWarnings("unchecked") Class<? extends Page>... additionalPages);

	void clearAuthenticationState();

	void decorateAuthenticationPage(Document content);

	void reportAuthenticationFailure(AuthenticationState state, Page page);

	Class<? extends Page> getAuthenticationPage(String authenticator);

	void validateModules(AuthenticationPolicy policy);

	Iterable<AuthenticationModule> getAuthenticationModules();

	AuthenticationModule getAuthenticationModule(String uuid);

	void assertLoginThesholds(String username, String remoteAddress);

	@SuppressWarnings("unchecked")
	void registerAuthenticationPage(AuthenticationModule module, Class<? extends AuthenticationPage<?>>... pages);

	AuthenticationState createAuthenticationState(AuthenticationPolicy policy) throws FileNotFoundException;

	AuthenticationState createAuthenticationState(AuthenticationPolicy policy, Redirect homePage) throws FileNotFoundException;

	AuthenticationState createAuthenticationState() throws FileNotFoundException;

	AuthenticationState createAuthenticationState(Redirect redirect) throws FileNotFoundException;

	AuthenticationState createAuthenticationState(AuthenticationPolicy policy, Redirect homePage, User user) throws FileNotFoundException;

	void changePolicy(AuthenticationState state, AuthenticationPolicy assigned, boolean verifiedPassword);
	
}

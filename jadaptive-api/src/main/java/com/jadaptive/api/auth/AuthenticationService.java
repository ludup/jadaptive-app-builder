package com.jadaptive.api.auth;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.jsoup.nodes.Document;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

public interface AuthenticationService {
	
	public abstract class LogonCompletedResult implements HttpSessionBindingListener, Closeable {
		private Optional<Session> session;
		
		public LogonCompletedResult(Optional<Session> session) {
			super();
			this.session = session;
		}
		
		public Optional<Session> session() {
			return session;
		}
		
	    public final void valueUnbound(HttpSessionBindingEvent event) {
	    	close();
	    }
	    
	    public abstract void close();
	}
	
	public abstract class AuthenticationCompletedResult extends LogonCompletedResult {
		private Redirect page;
		
		public AuthenticationCompletedResult(Redirect page, Optional<Session> session) {
			super(session);
			this.page = page;
		}

		public Redirect maybeAttachToSession(HttpServletRequest request, int timeoutMinutes) {
			session().ifPresent(session -> { 
				Session.set(request, AuthenticationCompletedResult.this);
				request.getSession().setMaxInactiveInterval(timeoutMinutes * 60);
			});
			return page;
		}
		
	}
	
	public static final String USER_LOGIN_PERMISSION =  "users.login";
	public static final String ALTERNATIVE_PASSWORD = "alternativePassword";
	public static final String AUTHENTICATION_STATE_ATTR = "authenticationState";
	public static final String DEFAULT_AUTHENTICATION_FLOW = "defaultAuthentication";
	public static final String PASSWORD = "password";
	
	public static final String PASSWORD_MODULE_UUID = "b76a4b67-ac70-45c2-95ca-9d7e14b3f695";
	
	LogonCompletedResult logonUser(String username, String password, Tenant tenant, String remoteAddress, String userAgent);

	AuthenticationState getCurrentState() throws FileNotFoundException;

	AuthenticationCompletedResult completeAuthentication(AuthenticationState state, Optional<Page> page);

	Class<? extends Page> resetAuthentication(@SuppressWarnings("unchecked") Class<? extends Page>... additionalPages);

	Class<? extends Page> resetAuthentication(String authenticationFlow, @SuppressWarnings("unchecked") Class<? extends Page>... additionalPages);

	void clearAuthenticationState();

	void decorateAuthenticationPage(Document content);

	void reportAuthenticationFailure(AuthenticationState state, Page page);

	Class<? extends Page> getAuthenticationPage(String authenticator);

	void validateModules(AuthenticationPolicy policy);

	Iterable<AuthenticationModule> getAuthenticationModules();

	AuthenticationModule getAuthenticationModule(String uuid);

	@SuppressWarnings("unchecked")
	void registerAuthenticationPage(AuthenticationModule module, Class<? extends AuthenticationPage<?>>... pages);

	AuthenticationState createAuthenticationState(AuthenticationPolicy policy) throws FileNotFoundException;

	AuthenticationState createAuthenticationState(AuthenticationPolicy policy, Redirect homePage) throws FileNotFoundException;

	AuthenticationState createAuthenticationState() throws FileNotFoundException;

	AuthenticationState createAuthenticationState(Redirect redirect) throws FileNotFoundException;

	AuthenticationState createAuthenticationState(AuthenticationPolicy policy, Redirect homePage, User user) throws FileNotFoundException;

	void changePolicy(AuthenticationState state, AuthenticationPolicy assigned, boolean verifiedPassword);

	void assertLoginThesholds();
	
}

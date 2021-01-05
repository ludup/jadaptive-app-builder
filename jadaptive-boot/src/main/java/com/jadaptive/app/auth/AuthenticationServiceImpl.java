package com.jadaptive.app.auth;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ConfigHelper;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.app.ui.Login;

@Service
@Permissions(keys = { AuthenticationService.USER_LOGIN_PERMISSION }, defaultPermissions = { AuthenticationService.USER_LOGIN_PERMISSION } )
public class AuthenticationServiceImpl extends AuthenticatedService implements AuthenticationService {

	public static final String PASSWORD_RESOURCE_KEY = "password";
	public static final String USERNAME_RESOURCE_KEY = "username";

	private static final String DEFAULT_AUTHENTICATION_FLOW = "defaultAuthentication";
	
	static Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private SessionUtils sessionUtils; 
	
	Map<String,Class<? extends Page>> registeredAuthenticationPages = new HashMap<>();
	
	
	@PostConstruct
	private void postConstruct() {
		registerAuthenticationPage(PASSWORD_RESOURCE_KEY, Login.class);
//		registerAuthenticationPage(USERNAME_RESOURCE_KEY, SetIdentity.class);
	}
	
	@Override
	public void registerAuthenticationPage(String resourceKey, Class<? extends Page> page) {
		registeredAuthenticationPages.put(resourceKey, page);
	}
	
	@Override
	public void decorateAuthenticationPage(Document content) {
		AuthenticationState state = getCurrentState();
		
		if(state.canReset()) {
			Element el = content.selectFirst("#actions");
			if(Objects.nonNull(el)) {
				el.append("<a href=\"" + state.getResetURL() + "\"><small>" + state.getResetText() + "</small></a>");
			}
		}

		 if(!state.isDecorateWindow()) {
			Element el = content.selectFirst("header");
			if(Objects.nonNull(el)) {
				el.remove();
			}
			
			el = content.selectFirst("footer");
			if(Objects.nonNull(el)) {
				el.remove();
			}
		 }
		 
		 if(state.hasUser()) {	 
			Element el = content.selectFirst("#username");
			if(Objects.nonNull(el)) {
				el.val(state.getUser().getUsername());
				el.attr("readOnly", "true");
			}
		 }
	}
	
	@Override
	public void reportAuthenticationFailure(AuthenticationState state) {
		permissionService.setupSystemContext();
		
		try {

			state.incrementFailedAttempts();
			
			assertLoginThreshold(state.getAttemptedUsername());
			assertLoginThreshold(Request.get().getRemoteAddr());
		
		} finally {
			permissionService.clearUserContext();
		}
	}
	@Override
	public Session logonUser(String username, String password, 
			Tenant tenant, String remoteAddress, String userAgent) {
		
		permissionService.setupSystemContext();
		
		try {
			
			assertLoginThreshold(username);
			assertLoginThreshold(remoteAddress);
			
			User user = userService.getUser(username);
			if(Objects.isNull(user) ||!userService.supportsLogin(user)) {
				throw new AccessDeniedException("Bad username or password");
			}
			
			setupUserContext(user);
			
			try {
				
				assertPermission(USER_LOGIN_PERMISSION);
				
				if(!userService.verifyPassword(user, password.toCharArray())) {
					flagFailedLogin(username);
					flagFailedLogin(remoteAddress);
					
					if(log.isInfoEnabled()) {
						log.info("Flagged failed login from {} and {}", username, remoteAddress);
					}
					
					throw new AccessDeniedException("Bad username or password");
				}
				
				return sessionService.createSession(tenant, 
						user, remoteAddress, userAgent);
				
			} finally {
				clearUserContext();
			}
			
		
		} finally {
			permissionService.clearUserContext();
		}
	}
	
	private void assertLoginThreshold(String key) {
		Integer count = getCache().get(getCacheKey(key));
		if(Objects.nonNull(count)) {
			if(count > getFailedLoginThreshold()) {
				if(log.isInfoEnabled()) {
					log.info("Rejecting login due to too many failues from {}", key);
				}
				throw new AccessDeniedException("Too many failed login attempts");
			}
		}
	}
	
	private Integer getFailedLoginThreshold() {
		return 10;
	}
	
	private Cache<String, Integer> getCache() {
		return cacheService.getCacheOrCreate("failedLogings", 
				String.class, Integer.class, 
					CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
	}
	
	private String getCacheKey(String username) {
		return String.format("%s.%s", getCurrentTenant().getUuid(), username);
	}

	private void flagFailedLogin(String username) {
		Cache<String, Integer> cache = getCache();
		String cacheKey = getCacheKey(username);
		Integer count = cache.get(cacheKey);
		if(Objects.isNull(count)) {
			count = new Integer(0);
		}
		cache.put(cacheKey, ++count);
	}

	@Override
	public Class<? extends Page> completeAuthentication(AuthenticationState state) {
		
		if(Objects.isNull(state.getUser()) || !userService.supportsLogin(state.getUser())) {
			throw new AccessDeniedException("Invalid credentials");
		}
		
		if(state.completePage()) {
			
			setupUserContext(state.getUser());
			
			try {
				assertPermission(USER_LOGIN_PERMISSION);
				
				Session session = sessionService.createSession(getCurrentTenant(), 
						state.getUser(), state.getRemoteAddress(), state.getUserAgent());
				sessionUtils.addSessionCookies(Request.get(), Request.response(), session);
				
			} finally {
				clearUserContext();
			}
		}
		
		return state.getCurrentPage();
		
	}

	@Override
	public AuthenticationState getCurrentState() {
		
		AuthenticationState state = (AuthenticationState) Request.get().getSession().getAttribute(AUTHENTICATION_STATE_ATTR);
		if(Objects.isNull(state)) {
			state = new AuthenticationState();
			state.setRemoteAddress(Request.get().getRemoteAddr());
			state.setUserAgent(Request.get().getHeader(HttpHeaders.USER_AGENT));
			processRequiredAuthentication(state, DEFAULT_AUTHENTICATION_FLOW);
			
			Request.get().getSession().setAttribute(AUTHENTICATION_STATE_ATTR, state);
		}
		
		return state;
	}

	private void processRequiredAuthentication(AuthenticationState state, String authenticationFlow) {
		
		File authenticationProperties = new File(ConfigHelper.getConfFolder(), "authentication.properties");
		state.getAuthenticationPages().clear();
		try {
			if(authenticationProperties.exists()) {
				Properties properties = ApplicationProperties.loadPropertiesFile(authenticationProperties);
				
				String[] defaultAuthentications = properties.getProperty(authenticationFlow, PASSWORD_RESOURCE_KEY).split(",");
				for(String authenticaiton :defaultAuthentications) {
					state.getAuthenticationPages().add(registeredAuthenticationPages.get(authenticaiton));
				}
				
				return;
			} 
		} catch(IOException e) {
			log.error("Failed to load authenticaiton properties", e);
		}
			
		state.getAuthenticationPages().add(Login.class);
		
	}
	
	@Override
	public Class<? extends Page> resetAuthentication(@SuppressWarnings("unchecked") Class<? extends Page>... additionalPages) {
		return resetAuthentication(DEFAULT_AUTHENTICATION_FLOW, additionalPages);
	}
	@Override
	public Class<? extends Page> resetAuthentication(String authenticationFlow, @SuppressWarnings("unchecked") Class<? extends Page>... additionalClasses) {
		
		Request.get().getSession().removeAttribute(AUTHENTICATION_STATE_ATTR);
		AuthenticationState state = getCurrentState();
		processRequiredAuthentication(state, authenticationFlow);
		state.getAuthenticationPages().addAll(Arrays.asList(additionalClasses));
		return state.getCurrentPage();
	}

	@Override
	public void clearAuthenticationState() {
		Request.get().getSession().removeAttribute(AUTHENTICATION_STATE_ATTR);
	}
}

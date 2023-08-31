package com.jadaptive.app.auth;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.auth.AuthenticationModule;
import com.jadaptive.api.auth.AuthenticationPolicy;
import com.jadaptive.api.auth.AuthenticationPolicyService;
import com.jadaptive.api.auth.AuthenticationScope;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.auth.PostAuthenticatorPage;
import com.jadaptive.api.auth.events.AuthenticationFailedEvent;
import com.jadaptive.api.auth.events.AuthenticationSuccessEvent;
import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionType;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.pages.auth.Login;
import com.jadaptive.api.ui.pages.auth.OptionalAuthentication;
import com.jadaptive.api.ui.pages.auth.Password;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
@Permissions(keys = { AuthenticationService.USER_LOGIN_PERMISSION }, defaultPermissions = {
		AuthenticationService.USER_LOGIN_PERMISSION })
public class AuthenticationServiceImpl extends AuthenticatedService implements AuthenticationService, StartupAware {

	public static final String USERNAME_RESOURCE_KEY = "username";

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

	@Autowired
	private PageCache pageCache;

	@Autowired
	private ApplicationService applicationService;
	
	@Autowired
	private TenantAwareObjectDatabase<AuthenticationModule> moduleDatabase;

	@Autowired
	private AuthenticationPolicyService policyService;

	@Autowired
	private EventService eventService; 
	
//	@Autowired
//	private UserDetailsService springUsers;
	
	Map<String, Class<? extends Page>> registeredAuthenticationPages = new HashMap<>();

	Map<Class<? extends Page>, AuthenticationModule> registeredModulesByPage = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public void registerAuthenticationPage(AuthenticationModule module, Class<? extends AuthenticationPage<?>>... pages) {
		if(Objects.isNull(pages) || pages.length == 0) {
			throw new IllegalArgumentException();
		}
		registeredAuthenticationPages.put(module.getAuthenticatorKey(), pages[0]);
		for(var c : pages) {
			registeredModulesByPage.put(c, module);
		}
	}

	@Override
	public void decorateAuthenticationPage(Document content) {
		AuthenticationState state = getCurrentState();

		if (state.canReset()) {
			Element el = content.selectFirst("#actions");
			if (Objects.nonNull(el)) {
				el.append("<a href=\"" + state.getResetURL() + "\"><small>" + state.getResetText() + "</small></a>");
			}
		}

		if (!state.isDecorateWindow()) {
			Element el = content.selectFirst("header");
			if (Objects.nonNull(el)) {
				el.remove();
			}

			el = content.selectFirst("footer");
			if (Objects.nonNull(el)) {
				el.remove();
			}
		}

		if (state.hasUser()) {
			Element el = content.selectFirst("#username");
			if (Objects.nonNull(el)) {
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

			flagFailedLogin(state.getAttemptedUsername());
			flagFailedLogin(Request.getRemoteAddress());

			if(!state.isFirstPage() || (state.isFirstPage() && state.getPolicy().getPasswordOnFirstPage())) {
				AuthenticationModule module = registeredModulesByPage.get(state.getCurrentPage());
				eventService.publishEvent(new AuthenticationFailedEvent(module, 
						state.getAttemptedUsername(),
						"", Request.getRemoteAddress()));
			}
		} finally {
			permissionService.clearUserContext();
		}
	}

	@Override
	public Session logonUser(String username, String password, Tenant tenant, String remoteAddress, String userAgent) {

		permissionService.setupSystemContext();

		try {

			User user = userService.getUser(username);
			if (Objects.isNull(user) || !userService.supportsLogin(user)) {
				throw new AccessDeniedException("Bad username or password");
			}

			setupUserContext(user);

			try {

				assertPermission(USER_LOGIN_PERMISSION);

				if (!userService.verifyPassword(user, password.toCharArray())) {
					flagFailedLogin(username);
					flagFailedLogin(remoteAddress);

					if (log.isInfoEnabled()) {
						log.info("Flagged failed login from {} and {}", username, remoteAddress);
					}

					throw new AccessDeniedException();
				}

				return sessionService.createSession(tenant, user, remoteAddress, userAgent, SessionType.HTTPS);

			} finally {
				clearUserContext();
			}

		} finally {
			permissionService.clearUserContext();
		}
	}
	
	@Override
	public void assertLoginThesholds(String username, String remoteAddress) {
		assertLoginThreshold(username);
		assertLoginThreshold(remoteAddress);
	}

	private void assertLoginThreshold(String key) {
		Integer count = getCache().get(getCacheKey(key));
		if (Objects.nonNull(count)) {
			if (count > getFailedLoginThreshold()) {
				if (log.isInfoEnabled()) {
					log.info("Rejecting login due to too many failues from {}", key);
				}
				throw new AccessDeniedException("Too many failed login attempts for " + key);
			}
		}
	}

	private Integer getFailedLoginThreshold() {
		return 5;
	}

	private Map<String, Integer> getCache() {
		return cacheService.getCacheOrCreate("failedLogings", String.class, Integer.class,
				TimeUnit.MINUTES.toMillis(5));
	}

	private String getCacheKey(String username) {
		return String.format("%s.%s", getCurrentTenant().getUuid(), username);
	}

	private void flagFailedLogin(String username) {
		Map<String, Integer> cache = getCache();
		String cacheKey = getCacheKey(username);
		Integer count = cache.get(cacheKey);
		if (Objects.isNull(count)) {
			count = Integer.valueOf(0);
		}
		cache.put(cacheKey, ++count);
	}

	@Override
	public Class<? extends Page> completeAuthentication(AuthenticationState state) {

		if (Objects.isNull(state.getUser()) || !userService.supportsLogin(state.getUser())) {
			throw new AccessDeniedException("Invalid credentials");
		}

		
		if(!state.isFirstPage() || (state.isFirstPage() && state.getPolicy().getPasswordOnFirstPage())) {
			AuthenticationModule module = registeredModulesByPage.get(state.getCurrentPage());
			if(Objects.nonNull(module)) {
				eventService.publishEvent(new AuthenticationSuccessEvent(module, 
						Objects.nonNull(state.getUser()) ? state.getUser().getUsername() : state.getAttemptedUsername(),
						Objects.nonNull(state.getUser()) ? state.getUser().getName() : "", Request.getRemoteAddress()));
			}
		}
		
		if (state.completePage()) {

			if(state.isAuthenticationComplete() && state.isOptionalComplete() && !state.hasPostAuthentication()) {
				setupPostAuthentication(state);
			}
			
			if(!state.hasPostAuthentication()) {
				if(state.getPolicy().isSessionRequired()) {
					createSession(state);
				}
				
//				if(state.getScope() == AuthenticationScope.SAML_IDP) {
//					DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) Request.get().getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
//				    
//					if(defaultSavedRequest != null){
//				    	AbstractAuthenticationToken auth = 
//								  new NoAuthAuthenticationToken(new IDPUserDetails(state.getUser(),
//										  buildAttributes(state.getUser())), getAuthorities());
//				    	auth.setDetails(new WebAuthenticationDetails(Request.get()));
//				    	auth.setAuthenticated(true);
//				    	
//				    	SecurityContext context = SecurityContextHolder.createEmptyContext(); 
//				    	context.setAuthentication(auth); 
//				    	SecurityContextHolder.setContext(context);
//				    	
//				    	HttpSession session = Request.get().getSession(false);
//						if (session != null) {
//							session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
//						}
//						session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
//				    	String targetURL = defaultSavedRequest.getRedirectUrl();
//				        throw new UriRedirect(targetURL);
//				    }
//				}
			}
			
		}

		return state.getCurrentPage();

	}
	
//	private List<Attribute> buildAttributes(User user) {
//		
//		var tmp = new ArrayList<Attribute>();
//		List<Attribute> attrs = new ArrayList<>();
//        attrs.add(new Attribute().setName("emailAddress").setValues(Collections.singletonList(user.getEmail())));
//        attrs.add(new Attribute().setName("name").setValues(Collections.singletonList(user.getName())));
//        
//        List<String> roles = new ArrayList<String>();
//        for(com.jadaptive.api.role.Role role : roleService.getRolesByUser(user)) {
//        	roles.add(role.getName());
//        }
//        attrs.add(new Attribute().setName("roles").addValues(roles));
//        return tmp;
//	}
//
//	
//	public Collection<GrantedAuthority> getAuthorities() {
//        //make everyone ROLE_USER
//        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
//        GrantedAuthority grantedAuthority = new GrantedAuthority() {
//            //anonymous inner type
//            public String getAuthority() {
//                return "ROLE_USER";
//            }
//        }; 
//        grantedAuthorities.add(grantedAuthority);
//        return grantedAuthorities;
//    }


	private void createSession(AuthenticationState state) {
		
		setupUserContext(state.getUser());

		try {
			assertPermission(USER_LOGIN_PERMISSION);

			Session session = sessionService.createSession(getCurrentTenant(), state.getUser(),
					state.getRemoteAddress(), state.getUserAgent(), SessionType.HTTPS);
			sessionUtils.addSessionCookies(Request.get(), Request.response(), session);

		} finally {
			clearUserContext();
		}
	}

	private void setupPostAuthentication(AuthenticationState state) {
		
		List<PostAuthenticatorPage> additional = new ArrayList<>();
		for(PostAuthenticatorPage a : applicationService.getBeans(PostAuthenticatorPage.class)) {
			if(a.getScope()==state.getPolicy().getScope()) {
				if(a.requiresProcessing(state)) {
					additional.add(a);
				}
			}
		}
		
		if(!additional.isEmpty()) {
			Collections.sort(additional, new Comparator<PostAuthenticatorPage>() {

				@Override
				public int compare(PostAuthenticatorPage o1, PostAuthenticatorPage o2) {
					return o1.getWeight().compareTo(o2.getWeight());
				}
			});
			
			state.getPostAuthenticationPages().clear();
			state.getPostAuthenticationPages().addAll(additional);
		}

	}

	@Override
	public AuthenticationState getCurrentState() {

		try {

			AuthenticationState state = (AuthenticationState) Request.get().getSession()
					.getAttribute(AUTHENTICATION_STATE_ATTR);
			if (Objects.isNull(state)) {


//				DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) Request.get().getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
			    
//				AuthenticationScope scope = defaultSavedRequest==null? 
//						AuthenticationScope.USER_LOGIN :
//							AuthenticationScope.SAML_IDP;
				
				AuthenticationPolicy policy = policyService.getDefaultPolicy(AuthenticationScope.USER_LOGIN);
				state = new AuthenticationState(AuthenticationScope.USER_LOGIN,
						policy,
						new PageRedirect(pageCache.getHomePage()));
				state.setRemoteAddress(Request.getRemoteAddress());
				state.setUserAgent(Request.get().getHeader(HttpHeaders.USER_AGENT));

				state.getRequiredPages().add(Login.class);

				Request.get().getSession().setAttribute(AUTHENTICATION_STATE_ATTR, state);
			}

			return state;
		} catch (FileNotFoundException | AccessDeniedException e1) {
			throw new IllegalStateException();
		}

	}

	@Override
	public void processRequiredAuthentication(AuthenticationState state, AuthenticationPolicy policy)
			throws FileNotFoundException {

		state.getRequiredPages().clear();
		state.getRequiredPages().add(Login.class);

		state.getOptionalAuthentications().clear();
		state.setOptionalCompleted(0);
		state.setOptionalRequired(policy.getOptionalRequired());
		state.setOptionalSelectionPage(OptionalAuthentication.class);

		state.setPolicy(policy);
		
		validateModules(policy, state.getRequiredPages(), state.getOptionalAuthentications());

	}

	@Override
	public void validateModules(AuthenticationPolicy policy) {
		validateModules(policy, new ArrayList<>(), new ArrayList<>());
	}

	private void validateModules(AuthenticationPolicy policy, List<Class<? extends Page>> required,
			List<AuthenticationModule> optional) {

		boolean hasSecret = false;

		for (AuthenticationModule module : policy.getRequiredAuthenticators()) {
			hasSecret |= module.isSecretCapture();
			required.add(getAuthenticationPage(module.getAuthenticatorKey()));
		}

		optional.addAll(policy.getOptionalAuthenticators());

		if (optional.size() < policy.getOptionalRequired()) {
			throw new IllegalStateException(
					"Invalid authentication policy! Minumum number of optional factors exceeds available optional factors");
		}

		if (!hasSecret && policy.getOptionalRequired() == 0) {
			throw new IllegalStateException("Invalid authentication policy! No secret capture modules are configured");
		}

		if (required.isEmpty() && (optional.isEmpty() || policy.getOptionalRequired() == 0)) {
			throw new IllegalStateException("Invalid authentication policy! No valid modules");
		}
	}

	@Override
	public Class<? extends Page> resetAuthentication(
			@SuppressWarnings("unchecked") Class<? extends Page>... additionalPages) {
		return resetAuthentication(DEFAULT_AUTHENTICATION_FLOW, additionalPages);
	}

	@Override
	public Class<? extends Page> resetAuthentication(String authenticationFlow,
			@SuppressWarnings("unchecked") Class<? extends Page>... additionalClasses) {

		Request.get().getSession().removeAttribute(AUTHENTICATION_STATE_ATTR);
		AuthenticationState state = getCurrentState();

		state.getRequiredPages().addAll(Arrays.asList(additionalClasses));
		return state.getCurrentPage();
	}

	@Override
	public void clearAuthenticationState() {
		Request.get().getSession().removeAttribute(AUTHENTICATION_STATE_ATTR);
	}

	@Override
	public Class<? extends Page> getAuthenticationPage(String authenticator) {
		if (registeredAuthenticationPages.containsKey(authenticator)) {
			return registeredAuthenticationPages.get(authenticator);
		}
		throw new IllegalStateException(String.format("%s is not an installed authenticator", authenticator));
	}

	@Override
	public AuthenticationModule getAuthenticationModule(String uuid) {
		return moduleDatabase.get(uuid, AuthenticationModule.class);
	}

	@Override
	public Iterable<AuthenticationModule> getAuthenticationModules() {
		return moduleDatabase.list(AuthenticationModule.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationStartup() {
		registerAuthenticationPage(getAuthenticationModule(PASSWORD_MODULE_UUID), Password.class, Login.class);
	}
	
//	class NoAuthAuthenticationToken extends AbstractAuthenticationToken {
//
//		private static final long serialVersionUID = 5824719286066761500L;
//		
//		UserDetails user;
//		
//		public NoAuthAuthenticationToken(UserDetails user, Collection<? extends GrantedAuthority> authorities) {
//			super(authorities);
//			this.user = user;
//		}
//
//		@Override
//		public Object getCredentials() {
//			return null;
//		}
//
//		@Override
//		public Object getPrincipal() {
//			return user;
//		}
//
//		public boolean isAuthenticated() { return true; }
//		
//	}
}

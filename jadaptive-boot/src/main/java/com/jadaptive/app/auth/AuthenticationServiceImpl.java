package com.jadaptive.app.auth;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

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
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationState;
import com.jadaptive.api.auth.AuthenticatorPage;
import com.jadaptive.api.auth.PostAuthenticatorPage;
import com.jadaptive.api.auth.UserLoginAuthenticationPolicy;
import com.jadaptive.api.auth.events.AuthenticationFailedEvent;
import com.jadaptive.api.auth.events.AuthenticationSuccessEvent;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.quotas.IPQuota;
import com.jadaptive.api.quotas.QuotaService;
import com.jadaptive.api.quotas.QuotaThreshold;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionType;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.ui.AuthenticationPage;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.Page;
import com.jadaptive.api.ui.PageCache;
import com.jadaptive.api.ui.PageRedirect;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.ui.pages.auth.Login;
import com.jadaptive.api.ui.pages.auth.OptionalAuthentication;
import com.jadaptive.api.ui.pages.auth.Password;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
@Permissions(keys = { AuthenticationService.USER_LOGIN_PERMISSION }, defaultPermissions = {
		AuthenticationService.USER_LOGIN_PERMISSION })
public class AuthenticationServiceImpl extends AuthenticatedService implements AuthenticationService, StartupAware, TenantAware {

	public static final String USERNAME_RESOURCE_KEY = "username";

	private static final String FAILED_LOGIN_ATTEMPTS_UUID = "8302adcf-a308-426d-a491-aabef5b78bf4";

	static Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

	@Autowired
	private UserService userService;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private PermissionService permissionService;

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
	
	@Autowired
	private QuotaService quotaService;
	
	Map<String, Class<? extends Page>> registeredAuthenticationPages = new HashMap<>();

	Map<Class<? extends Page>, AuthenticationModule> registeredModulesByPage = new HashMap<>();
	Map<String,AuthenticationModule> registeredModulesByResourceKey = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	@Override
	public void registerAuthenticationPage(AuthenticationModule module, Class<? extends AuthenticationPage<?>>... pages) {
		if(Objects.isNull(pages) || pages.length == 0) {
			throw new IllegalArgumentException();
		}
		registeredAuthenticationPages.put(module.getAuthenticatorKey(), pages[0]);
		registeredModulesByResourceKey.put(module.getAuthenticatorKey(), module);
		for(var c : pages) {
			registeredModulesByPage.put(c, module);
		}
	}

	@Override
	public void decorateAuthenticationPage(Document content) {
		AuthenticationState state = getCurrentState();

		if (state.canReset()) {
			
			Element el = content.selectFirst("#actions");
			if(Objects.nonNull(el)) {
				el.appendChild(Html.a(state.getResetURL())
					.addClass("text-decoration-none d-block")
					.appendChild(new Element("sup")
							.appendChild(Html.i18n(AuthenticationPolicy.RESOURCE_KEY, "resetLogin.text"))));
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

	}

	@Override
	public void reportAuthenticationFailure(AuthenticationState state, Page page) {
		permissionService.setupSystemContext();

		try {

			state.incrementFailedAttempts();

			flagFailedLogin();

			if(!state.isFirstPage() || (state.isFirstPage() && state.getPolicy().getPasswordOnFirstPage())) {
				Class<? extends Page> currentPage = state.getCurrentPage().orElseGet(() -> pageCache.getHomeClass());
				AuthenticationModule module = registeredModulesByPage.get(currentPage);
				if(Objects.isNull(module)) {
					log.warn("User failed authentication on page {} but no module is present!!!", currentPage.getSimpleName());
					return;
				}
				eventService.publishEvent(new AuthenticationFailedEvent(module, 
						state.getAttemptedUsername(),
						"", Request.getRemoteAddress()));
			}
		} finally {
			permissionService.clearUserContext();
		}
	}

	@Override
	public LogonCompletedResult logonUser(String username, String password, Tenant tenant, String remoteAddress, String userAgent) {

		permissionService.setupSystemContext();

		try {

			User user = userService.getUser(username);
			if (Objects.isNull(user) || !userService.supportsLogin(user)) {
				throw new AccessDeniedException("Bad username or password");
			}

			setupUserContext(user);

			try {

				assertPermission(USER_LOGIN_PERMISSION);

				assertLoginThesholds();
				
				if (!userService.verifyPassword(user, password.toCharArray())) {
					flagFailedLogin();

					if (log.isInfoEnabled()) {
						log.info("Flagged failed login from {}", remoteAddress);
					}

					throw new AccessDeniedException();
				}

				return new LogonCompletedResult(Optional.of(sessionService.createSession(tenant, user, remoteAddress, userAgent, SessionType.HTTPS, null))) {
					@Override
					public void close() {
						sessionService.closeSession(session().orElseThrow(() -> new IllegalStateException("Already closed.")));
					}
				};

			} finally {
				clearUserContext();
			}

		} finally {
			permissionService.clearUserContext();
		}
	}
	
	@Override
	public void assertLoginThesholds() {
		QuotaThreshold quota = quotaService.getAssignedThreshold(quotaService.getKey(FAILED_LOGIN_ATTEMPTS_UUID));
		quotaService.assertQuota(quota, AuthenticationPolicy.RESOURCE_KEY, "failedLoginThreshold.error");
	}
	
	private void flagFailedLogin() {
		QuotaThreshold quota = quotaService.getAssignedThreshold(quotaService.getKey(FAILED_LOGIN_ATTEMPTS_UUID));
		quotaService.incrementQuota(quota, AuthenticationPolicy.RESOURCE_KEY, "failedLoginThreshold.error");
	}

	@Override
	public AuthenticationCompletedResult completeAuthentication(AuthenticationState state, Optional<Page> page) {

		if (Objects.isNull(state.getUser()) || !userService.supportsLogin(state.getUser())) {
			throw new AccessDeniedException("Invalid credentials");
		}
		
		assertLoginThesholds();
		
		if(page.isPresent()) {
			Page p = page.get();
			if(state.isFirstPage() && state.getPolicy().getPasswordOnFirstPage()
					|| (p instanceof AuthenticatorPage 
							&& PASSWORD_MODULE_UUID.equals(((AuthenticatorPage)p).getAuthenticatorUUID()))) {
				
				AuthenticationModule module = getPasswordAuthenticationModule();
				eventService.publishEvent(new AuthenticationSuccessEvent(module, 
						Objects.nonNull(state.getUser()) ? state.getUser().getUsername() : state.getAttemptedUsername(),
						Objects.nonNull(state.getUser()) ? state.getUser().getName() : "", Request.getRemoteAddress()));
				
			} else if(p instanceof AuthenticatorPage) {
				AuthenticationModule module = moduleDatabase.get(((AuthenticatorPage)p).getAuthenticatorUUID(), AuthenticationModule.class);
				eventService.publishEvent(new AuthenticationSuccessEvent(module, 
						Objects.nonNull(state.getUser()) ? state.getUser().getUsername() : state.getAttemptedUsername(),
						Objects.nonNull(state.getUser()) ? state.getUser().getName() : "", Request.getRemoteAddress()));
			}
		}
		
		if (state.completePage()) {
			
			if(log.isInfoEnabled()) {
				log.info("User {} has completed authentication", state.getUser().getUsername());
			}
			if(!state.hasPostAuthentication()) {
			
				if(log.isInfoEnabled()) {
					log.info("User {} has completed POST authentication", state.getUser().getUsername());
				}
				
				if(state.getPolicy().isSessionRequired()) {
					
					if(log.isInfoEnabled()) {
						log.info("Creating session for {}", state.getUser().getUsername());
					}
					setupUserContext(state.getUser());

					try {
						assertPermission(USER_LOGIN_PERMISSION);
						Session session = sessionService.createSession(getCurrentTenant(), state.getUser(),
								state.getRemoteAddress(), state.getUserAgent(), SessionType.HTTPS, state);
						Redirect redir;
						try {
							redir = new PageRedirect(pageCache.getPage(state.getCurrentPage().orElseGet(() -> pageCache.getHomeClass())));
						}
						catch(Redirect r) {
							redir = r;
						}
						return new AuthenticationCompletedResult(
								redir, 
								Optional.of(session)
						) {
							@Override
						    public void close() {
								sessionService.closeSession(session().orElseThrow(() -> new IllegalStateException("Already closed.")));
						    }
						};
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					finally {
						clearUserContext();
					}
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
		
		Class<? extends Page> currentPage = state.getCurrentPage().orElseGet(() -> pageCache.getHomeClass());
		if(log.isInfoEnabled()) {
			log.info("User {} is being asked to complete authenication page {}", 
					state.getUser().getUsername(),
					currentPage.getSimpleName());
		}

		if(AuthenticationPage.class.isAssignableFrom(currentPage)) {
			AuthenticationPage<?> nextPage = (AuthenticationPage<?>) pageCache.getPage(currentPage);
			if(!nextPage.canAuthenticate(state)) {
				throw new ObjectException(
						AuthenticationPolicy.RESOURCE_KEY,
						"missingCredentials.text");
			}
		}
		
		return new AuthenticationCompletedResult(new PageRedirect(pageCache.getPage(currentPage)), Optional.empty()) {
			@Override
			public void close() {
				throw new IllegalStateException("Session was never open.");
			}
		};

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


	private AuthenticationModule getPasswordAuthenticationModule() {
		
		AuthenticationModule m = new AuthenticationModule();
		m.setUuid(PASSWORD_MODULE_UUID);
		m.setAuthenticatorKey(PASSWORD);
		m.setIdentityCapture(false);
		m.setSecretCapture(true);
		m.setName("Password");
		m.setRequiresEmailAddress(false);
		m.setRequiresPhoneNumber(false);
		m.setSystem(true);
		
		return m;
	}	

	private void setupPostAuthentication(AuthenticationState state) {
		
		List<PostAuthenticatorPage> additional = new ArrayList<>();
		for(PostAuthenticatorPage a : applicationService.getBeans(PostAuthenticatorPage.class)) {
			if(a.requiresProcessing(state)) {
				additional.add(a);
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

			HttpSession httpSession = Request.get().getSession();
			AuthenticationState state = (AuthenticationState) httpSession
					.getAttribute(AUTHENTICATION_STATE_ATTR);
			if (Objects.isNull(state)) {


//				DefaultSavedRequest defaultSavedRequest = (DefaultSavedRequest) Request.get().getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
			    
//				AuthenticationScope scope = defaultSavedRequest==null? 
//						AuthenticationScope.USER_LOGIN :
//							AuthenticationScope.SAML_IDP;
				
				state = createAuthenticationState();
				
				processRequiredAuthentication(state, state.getPolicy());
			}

			return state;
		} catch (FileNotFoundException | AccessDeniedException e1) {
			throw new IllegalStateException(e1);
		}

	}

	@Override
	public AuthenticationState createAuthenticationState() throws FileNotFoundException {				
		return createAuthenticationState(policyService.getDefaultPolicy(UserLoginAuthenticationPolicy.class));
	}

	@Override
	public AuthenticationState createAuthenticationState(Redirect redirect) throws FileNotFoundException {				
		return createAuthenticationState(policyService.getDefaultPolicy(UserLoginAuthenticationPolicy.class), redirect);
	}
	
	@Override 
	public void changePolicy(AuthenticationState state, AuthenticationPolicy policy, boolean verifiedPassword) {
		
		boolean hasLoginPage = state.getRequiredPages().contains(Login.class);
		state.clearRequiredAuthentications();
		
		if(policy.getPasswordOnFirstPage() || Objects.isNull(state.getUser()) || hasLoginPage) {
			state.getRequiredPages().add(Login.class);
		}
		
		if(!verifiedPassword) {
			if(policy.getPasswordRequired() && !policy.getPasswordOnFirstPage()) {
				state.addRequiredAuthentication(Password.class, null);
			}
		}
		
		for(AuthenticationModule m : policy.getRequiredAuthenticators()) {
			state.addRequiredAuthentication(
					registeredAuthenticationPages.get(m.getAuthenticatorKey()),
					m);
		}
		
		state.setPasswordEnabled(policy.getPasswordOnFirstPage() || policy.getPasswordRequired() || policy.getPasswordProvided());
		state.clearOptionalAuthentications();
		for(AuthenticationModule m : policy.getOptionalAuthenticators()) {
			state.addOptionalAuthentication(getAuthenticationPage(m.getAuthenticatorKey()), m);
		}
		state.setOptionalCompleted(0);
		state.setOptionalRequired(Math.min(policy.getOptionalRequired(), state.getOptionalAuthentications().size()));
		state.setOptionalSelectionPage(OptionalAuthentication.class);

		state.setPolicy(policy);
		
		setupPostAuthentication(state);
		
		validateModules(policy);
	}

	private void processRequiredAuthentication(AuthenticationState state, 
			AuthenticationPolicy policy)
			throws FileNotFoundException {

		if(Objects.nonNull(state.getUser()) && !policy.isTemporary()) {
			AuthenticationPolicy assignedPolicy = policyService.getAssignedPolicy(state.getUser(),
					Request.getRemoteAddress(), 
					policy.getClass());
			if(Objects.nonNull(assignedPolicy)) {
				policy = assignedPolicy;
			}
		}
		
		if(policy.getPasswordOnFirstPage() || Objects.isNull(state.getUser())) {
			state.getRequiredPages().add(Login.class);
		}
		
		changePolicy(state, policy, false);

	}

	@Override
	public void validateModules(AuthenticationPolicy policy) {

		if(policy.getPasswordRequired() || policy.getPasswordProvided() || policy.isSecondaryOnly()) {
			return;
		}
		
		boolean hasSecret = false;

		for (AuthenticationModule module : policy.getRequiredAuthenticators()) {
			hasSecret |= module.isSecretCapture();
		}

		if (policy.getOptionalAuthenticators().size() < policy.getOptionalRequired()) {
			throw new IllegalStateException(
					"Invalid authentication policy! Minumum number of optional factors exceeds available optional factors");
		}

		if (!hasSecret && policy.getOptionalRequired() == 0) {
			throw new IllegalStateException("Invalid authentication policy! No secret capture modules are configured");
		}

		if (policy.getRequiredAuthenticators().isEmpty() && (policy.getOptionalAuthenticators().isEmpty() || policy.getOptionalRequired() == 0)) {
			throw new IllegalStateException("Invalid authentication policy! No valid modules");
		}
	}

//	@Override
//	public Class<? extends Page> resetAuthentication(
//			@SuppressWarnings("unchecked") Class<? extends Page>... additionalPages) {
//		return resetAuthentication(DEFAULT_AUTHENTICATION_FLOW, additionalPages);
//	}
//
//	@Override
//	public Class<? extends Page> resetAuthentication(String authenticationFlow,
//			@SuppressWarnings("unchecked") Class<? extends Page>... additionalClasses) {
//
//		Request.get().getSession().removeAttribute(AUTHENTICATION_STATE_ATTR);
//		AuthenticationState state = getCurrentState();
//
//		state.getRequiredPages().addAll(Arrays.asList(additionalClasses));
//		return state.getCurrentPage();
//	}

	@Override
	public void clearAuthenticationState() {
		Request.get().getSession().removeAttribute(AUTHENTICATION_STATE_ATTR);
	}

	@Override
	public Class<? extends Page> getAuthenticationPage(String authenticator) {
		if (registeredAuthenticationPages.containsKey(authenticator)) {
			return registeredAuthenticationPages.get(authenticator);
		}
		if(PASSWORD.equals(authenticator)) {
			return Password.class;
		}
		throw new IllegalStateException(String.format("%s is not an installed authenticator", authenticator));
	}

	@Override
	public AuthenticationModule getAuthenticationModuleByUUID(String uuid) {
		return moduleDatabase.get(uuid, AuthenticationModule.class);
	}
	
	@Override
	public AuthenticationModule getAuthenticationModuleByResourceKey(String resourceKey) {
		return registeredModulesByResourceKey.get(resourceKey);
	}

	@Override
	public Iterable<AuthenticationModule> getAuthenticationModules() {
		return moduleDatabase.list(AuthenticationModule.class);
	}

	@Override
	public void onApplicationStartup() {
		/**
		 * Removing this because we now specify password as a switch on policy
		 */
//		registerAuthenticationPage(getAuthenticationModule(PASSWORD_MODULE_UUID), Password.class, Login.class);
	}

	@Override
	public AuthenticationState createAuthenticationState(AuthenticationPolicy policy) throws FileNotFoundException {
		return createAuthenticationState(policy, null);
	}
	
	@Override
	public AuthenticationState createAuthenticationState(AuthenticationPolicy policy, Redirect homePage, User user) throws FileNotFoundException {
		AuthenticationState state = new AuthenticationState(policy);
		state.setHomePage(homePage);
		state.setRemoteAddress(Request.getRemoteAddress());
		state.setUser(user);
		state.setUserAgent(Request.get().getHeader(HttpHeaders.USER_AGENT));

		processRequiredAuthentication(state, policy);
		
		Request.get().getSession().setAttribute(AUTHENTICATION_STATE_ATTR, state);
		return state;
		
	}

	@Override
	public AuthenticationState createAuthenticationState(AuthenticationPolicy policy, Redirect homePage)
			throws FileNotFoundException {
		return createAuthenticationState(policy, homePage, null);
	}
	
	public void initializeSystem(boolean newSchema) {
		initializeTenant(getCurrentTenant(), newSchema);
	};
	
	public void initializeTenant(Tenant tenant, boolean newSchema) {
		
		if(moduleDatabase.count(AuthenticationModule.class, SearchField.eq("uuid", PASSWORD_MODULE_UUID)) > 0) {
			for(AuthenticationPolicy policy : policyService.allObjects()) {
				boolean remove = false;
				AuthenticationModule toRemove = null;
				for(AuthenticationModule m : policy.getRequiredAuthenticators()) {
					if(PASSWORD.equals(m.getAuthenticatorKey())) {
						remove = true;
						toRemove = m;
					}
				}
				if(remove) {
					policy.getRequiredAuthenticators().remove(toRemove);
					policy.setPasswordRequired(true);
				}
				remove = false;
				toRemove = null;
				for(AuthenticationModule m : policy.getOptionalAuthenticators()) {
					if(PASSWORD.equals(m.getAuthenticatorKey())) {
						remove = true;
						toRemove = m;
					}
				}
				if(remove) {
					policy.getOptionalAuthenticators().remove(toRemove);
				}
				policyService.saveOrUpdate(policy);
			}
			
			AuthenticationModule m = moduleDatabase.get(PASSWORD_MODULE_UUID, AuthenticationModule.class);
			m.setSystem(false);
			moduleDatabase.saveOrUpdate(m);
			moduleDatabase.delete(m);
		}
		
		if(!quotaService.hasKey(FAILED_LOGIN_ATTEMPTS_UUID)) {
			quotaService.registerKey(FAILED_LOGIN_ATTEMPTS_UUID, "Failed Login Attempts");
			IPQuota quota = new IPQuota();
			quota.setKey(quotaService.getKey(FAILED_LOGIN_ATTEMPTS_UUID));
			quota.setPeriodUnit(TimeUnit.MINUTES);
			quota.setPeriodValue(5);
			quota.setValue("5");
			quota.setAllAddresses(Boolean.TRUE);
			quota.setSystem(true);
			
			quotaService.createQuota(quota);
		}
	}

	@Override
	public Class<? extends Page> getCurrentPage() {
		return getCurrentState().getCurrentPage().orElseGet(() -> pageCache.getHomeClass());
	};
	
	
	
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

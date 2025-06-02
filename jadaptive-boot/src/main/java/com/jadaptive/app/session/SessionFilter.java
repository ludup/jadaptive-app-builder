package com.jadaptive.app.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.App;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.auth.AuthenticationService.LogonCompletedResult;
import com.jadaptive.api.auth.oauth2.OAuth2Token;
import com.jadaptive.api.auth.oauth2.OAuth2TokenService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SystemSingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.redirect.Redirect;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.PluginInterceptor;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantConfiguration;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.utils.ReplacementUtils;
import com.jadaptive.utils.StaticResolver;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = { "/*" }, dispatcherTypes = { DispatcherType.REQUEST })
public class SessionFilter implements Filter {

	static Logger log = LoggerFactory.getLogger(SessionFilter.class);
	
	public static final String PRE_LOGON_ORIGINAL_URL = "originalURL";

	private static final String ALLOW_HTTP = "enableHttp";

	private static final String DISABLE_DEFAULT_REDIRECT = "disableDefaultRedirect";
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionUtils sessionUtils; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private SecurityPropertyService securityService;  
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private App applicationService; 
	
	@Autowired
	private OAuth2TokenService oauth2TokenService; 
	
	@Autowired
	private TenantAwareObjectDatabase<Redirect> redirectDatabase;
	
	@Autowired
	private SystemSingletonObjectDatabase<TenantConfiguration> tenantConfig;
	
	private Map<String,String> cachedRedirects = new HashMap<>();
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;

		Request.setUp(req, resp);
		
		if(log.isDebugEnabled()) {
			log.debug(req.getMethod() + " " + req.getRequestURI().toString());
		}
		
		if(!tenantService.isReady()) {
			if(!req.getRequestURI().startsWith("/app/ui/startup") && !isContent(req)) {
				resp.sendRedirect("/app/ui/startup");
				return;
			}
		} 

		tenantService.setCurrentTenant(req);
		
		try {
			
			Tenant tenant = tenantService.getCurrentTenant();

			if(Objects.nonNull(tenant)) {
				String serverName = req.getServerName();
				TenantConfiguration config = tenantConfig.getObject(TenantConfiguration.class);
				String hostName;
				try {
					hostName = InetAddress.getLocalHost().getHostName();
				}
				catch(Exception e) {
					hostName = "localhost";
				}
				if(!isValidHostname(config, tenant, serverName) && !serverName.equals(hostName) ) {
					if(serverName.equalsIgnoreCase(config.getRegistrationDomain())) {
						if(req.getRequestURI().equals("/")) {
							if(req.getServerPort() != -1 && req.getServerPort() != 443) {
								resp.sendRedirect(String.format("https://%s:%d/app/ui/wizards/setupTenant", 
										config.getRegistrationDomain(),
										request.getServerPort()));
							} else {
								resp.sendRedirect(String.format("https://%s/app/ui/wizards/setupTenant", config.getRegistrationDomain()));
							}	
							return;
						}
					} else {
						if(config.getRequireValidDomain()) {
							String redir;
							if(StringUtils.isBlank(config.getInvalidDomainRedirect())) {
								if(StringUtils.isBlank(config.getRootDomain())) {
									resp.sendError(HttpServletResponse.SC_NOT_FOUND);
									return;
								}
								else {
									redir = "https://" + config.getRootDomain();	
								}
							}
							else {
								redir = config.getInvalidDomainRedirect();
							}

							URI redirUri = URI.create(redir);
							if(!redirUri.getHost().equals(serverName)) {
								resp.sendRedirect(redir);
								return;
							}
						}
					}
				}
			}
			
		
			Properties properties = securityService.resolveSecurityProperties(req.getRequestURI());

			if(checkRedirects(req, resp, properties)) {
				return;
			}
			
			sessionUtils.isValidCORSRequest(req, resp, properties);
			
			if(!preHandle(req, resp, properties)) {
				return;
			}
			
			chain.doFilter(req, resp);

			postHandle(req, resp);
			
		} catch(Throwable e) { 
			log.error("Filter handling failed.", e);
			throw e;
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

	private boolean isContent(HttpServletRequest req) {
		return req.getRequestURI().startsWith("/app/content/")
				|| req.getRequestURI().startsWith("/app/api/update-state")
				|| req.getRequestURI().startsWith("/app/api/ready")
				|| req.getRequestURI().startsWith("/app/css/")
				|| req.getRequestURI().startsWith("/app/js/")
				|| req.getRequestURI().startsWith("/favicon");
	}

	private boolean isValidHostname(TenantConfiguration config, Tenant tenant, String serverName) {
		return tenant.isValidHostname(serverName) || 
			 ( tenant.isSystem() && ( 
					serverName.equals(config.getRootDomain()) ||
					serverName.equals(tenant.getDomain()) ||
					serverName.equals(tenant.getHostname() + "." + config.getRootDomain())
			    )
			 );
	}

	private void postHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		try {
			for(PluginInterceptor in : applicationService.getBeans(PluginInterceptor.class)) {
				in.postHandle(request, response);
			}
			
		
		} catch(Throwable e) {
			log.error("Caught exception in SessionFilter postHandle",e);
			throw new ServletException(e);
		} finally {
			
			tenantService.clearCurrentTenant();
			if(permissionService.hasUserContext()) {
				permissionService.clearUserContext();
			}
			
			sessionUtils.populateSecurityHeaders(response);
			
			Session.getOr().ifPresent(s -> {
				var tkn = s.getAttribute(OAuth2Token.class);
				/* OAuth2 requests close session after every call */
				if(tkn != null) {
					s.getAttribute(LogonCompletedResult.class).close();
				}
			});
			
			Request.tearDown();
		}
		
	}
	



	private boolean preHandle(HttpServletRequest request, HttpServletResponse response, Properties properties) throws ServletException {
		
		try {
			if(request.getRequestURI().equals("/app/verify")) {
				/* We do not want this call to access the session */
				return true;
			}
			
			var sessionOr = Session.getOr(request);

			if(!request.isSecure()) {
				if(!Boolean.parseBoolean(properties.getProperty(ALLOW_HTTP))) {
					response.sendRedirect(request.getRequestURL().toString().replace("http:", "https:")
							.replace(ApplicationProperties.getValue("server.http.port", "80"), 
									ApplicationProperties.getValue("server.port", "443")));
					return true;
				}
			}
			
			var authHdr = request.getHeader(HttpHeaders.AUTHORIZATION);
			if(Objects.nonNull(authHdr)) {
				sessionOr = performHttpAuthentication(request, response, properties);
			}
			
			if(sessionOr.isEmpty() && Boolean.parseBoolean(properties.getProperty("authentication.allowAnonymous", "false"))) {
				permissionService.setupSystemContext();
			} else if(sessionOr.isPresent()) {
				var session = sessionOr.get();
				tenantService.setCurrentTenant(session.getTenant());	
				permissionService.setupUserContext(session.getUser());
			} 
		
			String requireAllPermission = StringUtils.defaultIfBlank(properties.getProperty("permission.requireAll"), null);
			String requireAnyPermission = StringUtils.defaultIfBlank(properties.getProperty("permission.requireAny"), null);
	
			if(Objects.nonNull(requireAllPermission)) {
				permissionService.assertAllPermission(requireAllPermission.split(","));
			}
			
			if(Objects.nonNull(requireAnyPermission)) {
				permissionService.assertAnyPermission(requireAnyPermission.split(","));
			}
			
			if(!iteratePluginInterceptors(request, response)) {
				return false;
			}
			
			return true;
		} catch(Throwable e) {
			log.error("Caught exception in SessionFilter preHandle",e);
			throw new ServletException(e);
		}
		
	}

	private boolean iteratePluginInterceptors(HttpServletRequest request, HttpServletResponse response) throws Exception {
		for(PluginInterceptor in : applicationService.getBeans(PluginInterceptor.class)) {
			if(!in.preHandle(request, response)) {
				return false;
			}
		}
		return true;
	}

	private Optional<Session> performHttpAuthentication(HttpServletRequest request, HttpServletResponse response, Properties properties) throws UnsupportedEncodingException {
		
		String[] authorization = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ");
		if(authorization.length > 1) {
			
			if(authorization[0].equalsIgnoreCase("Bearer")) {
				
				/* An OAuth authenticated API call is intended for a single use. The
				 * session will then be invalidated. Access to the API will further
				 * be restricted by scope later on when it is known (by a handler interceptor).
				 * 
				 * TODO invalidate the session
				 */
				Tenant tenant = tenantService.getCurrentTenant();
				var tkn = oauth2TokenService.byToken(authorization[1]);
				if(!tkn.getTenant().equals(tenant.getUuid())) {
					throw new IllegalStateException("Incorrect tenant.");
				}
				
				LogonCompletedResult result = authenticationService.logonUser(
						tkn.getOwner(), 
						tenantService.getCurrentTenant(), 
						Request.getRemoteAddress(), 
						request.getHeader(HttpHeaders.USER_AGENT));
				
				Session.set(request, result);
				result.session().get().setAttribute(OAuth2Token.class, tkn);
				result.session().get().setAttribute(LogonCompletedResult.class, result);
				
				return result.session();
			}
			
			if(Boolean.parseBoolean(properties.getProperty("authentication.allowBasic", "false")) && authorization[0].equalsIgnoreCase("BASIC")) {
				String encoded = new String(Base64.getDecoder().decode(authorization[1]), "UTF-8");
				int idx = encoded.indexOf(':');
				if(idx==-1) {
					return null;
				}
				String username = encoded.substring(0, idx);
				String password = encoded.substring(idx+1);
				
				LogonCompletedResult result = authenticationService.logonUser(
						username, 
						password, 
						tenantService.getCurrentTenant(), 
						Request.getRemoteAddress(), 
						request.getHeader(HttpHeaders.USER_AGENT));
				
				Session.set(request, result);
				
				return result.session();
			}
		}
		return Optional.empty();
	}
	
	private boolean checkRedirects(HttpServletRequest request, HttpServletResponse response, Properties properties) throws IOException {
		
		try {
				
			String location = cachedRedirects.get(request.getRequestURL().toString());
			
			if(StringUtils.isNotBlank(location)) {
				response.sendRedirect(location);
				return true;
			} else if(Objects.isNull(location)) {
				
				if(log.isDebugEnabled()) {
					log.debug("Checking redirect {}", request.getRequestURL().toString());
				}
				
				for(Redirect redirect : redirectDatabase.list(Redirect.class, 
						SearchField.or(SearchField.eq("hostname", request.getServerName()),
								SearchField.eq("hostname", null),
								SearchField.eq("hostname", "")))) {
					Pattern pattern = Pattern.compile(redirect.getPath());
					Matcher matcher = pattern.matcher(request.getRequestURI());
					if(matcher.matches()) {
						
						if(StringUtils.isNotBlank(redirect.getHostname())) {
							if(!request.getServerName().equals(redirect.getHostname())) {
								continue;
							}
						}
						
						location = redirect.getLocation();
						for(int i = 0; i <= matcher.groupCount(); i++) { 
							location = location.replace("$" + i, matcher.group(i));
						}
						
						StaticResolver resolver = new StaticResolver();
						resolver.addToken("version", ApplicationVersion.getVersion());
						resolver.addToken("host", request.getHeader(HttpHeaders.HOST));
						resolver.addToken("serverName", request.getServerName());
						
						
						location = ReplacementUtils.processTokenReplacements(location, resolver);
						cachedRedirects.put(request.getRequestURL().toString(), location);
						
						if(log.isDebugEnabled()) {
							log.debug("Redirecting {} to {}", request.getRequestURL().toString(), location);
						}
						response.sendRedirect(location);
						return true;
					}
				}
				
				cachedRedirects.put(request.getRequestURL().toString(), "");
			}
			
		} catch(ObjectNotFoundException e) {
		}
		
		if(!Boolean.parseBoolean(properties.getProperty(DISABLE_DEFAULT_REDIRECT, "false"))) {
			if(request.getRequestURI().equals("/")
					|| request.getRequestURI().equals("/app")
					|| request.getRequestURI().equals("/app/")) {
				response.sendRedirect("/app/ui/");
				return true;
			}
		}
		
		return false;
	}
}

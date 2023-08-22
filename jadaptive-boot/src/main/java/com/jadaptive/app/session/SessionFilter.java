package com.jadaptive.app.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.auth.AuthenticationService;
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
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantConfiguration;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.utils.ReplacementUtils;
import com.jadaptive.utils.StaticResolver;

@WebFilter(urlPatterns = { "/*" }, dispatcherTypes = { DispatcherType.REQUEST })
public class SessionFilter implements Filter {

	static Logger log = LoggerFactory.getLogger(SessionFilter.class);
	
	public static final String PRE_LOGON_ORIGINAL_URL = "originalURL";
	
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
	private ApplicationService applicationService; 
	
	@Autowired
	private TenantAwareObjectDatabase<Redirect> redirectDatabase;
	
	@Autowired
	private SystemSingletonObjectDatabase<TenantConfiguration> tenantConfig;
	
	Map<String,String> cachedRedirects = new HashMap<>();
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;

		Request.setUp(req, resp);
		
		if(log.isDebugEnabled()) {
			log.debug(req.getMethod() + " " + req.getRequestURI().toString());
		}
		
		tenantService.setCurrentTenant(req);
		
		try {
			Tenant tenant = tenantService.getCurrentTenant();

			if(Objects.nonNull(tenant)) {
				if(!tenant.isValidHostname(request.getServerName())) {
					TenantConfiguration config = tenantConfig.getObject(TenantConfiguration.class);
					if(config.getRequireValidDomain()) {
						if(req.getServerName().equalsIgnoreCase(config.getRegistrationDomain())) {
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
							resp.sendRedirect(config.getInvalidDomainRedirect());
							return;
						}
					}
				}
			}

			if(checkRedirects(req, resp)) {
				return;
			}
			
			if(!preHandle(req, resp)) {
				return;
			}
			
			chain.doFilter(req, resp);

			postHandle(req, resp);
			
		} catch(Throwable e) { 
			e.printStackTrace();
			throw e;
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

	private void postHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		try {
			for(PluginInterceptor in : applicationService.getBeans(PluginInterceptor.class)) {
				in.postHandle(request, response);
			}
			
			tenantService.clearCurrentTenant();
			if(permissionService.hasUserContext()) {
				permissionService.clearUserContext();
			}
			
			Request.tearDown();
		
		} catch(Throwable e) {
			log.error("Caught exception in SessionFilter postHandle",e);
			throw new ServletException(e);
		}
		
	}
	



	private boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		try {
			
			Session session = null;
			
			try {
				session = sessionUtils.getSession(request);
			} catch (UnauthorizedException e) {
			}
			
			sessionUtils.populateSecurityHeaders(response);
			
			/**
			 * Get the security.properties hierarchy from the web application
			 */
			Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
			
			if(Boolean.parseBoolean(properties.getProperty("authentication.allowBasic", "false"))
					&& Objects.nonNull(request.getHeader(HttpHeaders.AUTHORIZATION))) {
				session = performBasicAuthentication(request, response);
			}
			
			if(Objects.isNull(session) && Boolean.parseBoolean(properties.getProperty("authentication.allowAnonymous", "false"))) {
				permissionService.setupSystemContext();
			} else if(Objects.nonNull(session)) {
				sessionUtils.touchSession(session);
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

	private Session performBasicAuthentication(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		
		String[] authorization = request.getHeader(HttpHeaders.AUTHORIZATION).split(" ");
		if(authorization.length > 1) {
			if(authorization[0].equalsIgnoreCase("BASIC")) {
				String encoded = new String(Base64.getDecoder().decode(authorization[1]), "UTF-8");
				int idx = encoded.indexOf(':');
				if(idx==-1) {
					return null;
				}
				String username = encoded.substring(0, idx);
				String password = encoded.substring(idx+1);
				
				Session session = authenticationService.logonUser(
						username, 
						password, 
						tenantService.getCurrentTenant(), 
						Request.getRemoteAddress(), 
						request.getHeader(HttpHeaders.USER_AGENT));
				
				sessionUtils.addSessionCookies(request, response, session);
				
				return session;
			}
		}
		return null;
	}
	
	private boolean checkRedirects(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
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
		
		if(request.getRequestURI().equals("/")
				|| request.getRequestURI().equals("/app")
				|| request.getRequestURI().equals("/app/")) {
			response.sendRedirect("/app/ui/");
			return true;
		}
		
		return false;
	}
}

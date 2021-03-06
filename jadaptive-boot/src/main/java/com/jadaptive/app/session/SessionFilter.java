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
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.redirect.Redirect;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.PluginInterceptor;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
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
	
	Map<String,String> cachedRedirects = new HashMap<>();
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		
		if(log.isDebugEnabled()) {
			log.debug(req.getMethod() + " " + req.getRequestURI().toString());
		}
		
		tenantService.setCurrentTenant(req);
		
		try {

			if(checkRedirects(req, resp)) {
				return;
			}
			
			if(!preHandle(req, resp)) {
				return;
			}
			
			chain.doFilter(request, response);
			
			postHandle(req, resp);
			
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
			throw new ServletException(e);
		}
		
	}

	private boolean preHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
		try {
			Request.setUp(request, response);
			
			Session session = sessionUtils.getActiveSession(request);
			
			/**
			 * Get the security.properties hierarchy from the web application
			 */
			Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
			
			/**
			 * Check if we have a valid CORS request
			 */
			@SuppressWarnings("unused")
			boolean validCORS = sessionUtils.isValidCORSRequest(request, response, properties);		
					
			if(Boolean.parseBoolean(properties.getProperty("authentication.allowBasic", "false"))
					&& Objects.nonNull(request.getHeader(HttpHeaders.AUTHORIZATION))) {
				session = performBasicAuthentication(request, response);
			}
			
			/**
			 * If the request is not a valid CORS then check we have valid CSRF token in the request.
			 * 
			 * THIS IS CURRENTLY DISABLED AS THE UI IS INCOMPLETE
			 */
			/**
			if(Objects.nonNull(session) && !validCORS 
					&& (ApplicationProperties.getValue("security.enableCSRFProtection", true) || 
							Boolean.parseBoolean(properties.getProperty("security.enableCSRFProtection", "true")))) {
				sessionUtils.verifySameSiteRequest(request, response, session);
			}**/
			
			
			if(Objects.isNull(session) && Boolean.parseBoolean(properties.getProperty("authentication.allowAnonymous", "false"))) {
				permissionService.setupSystemContext();
			} else if(Objects.nonNull(session)) {
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
						request.getRemoteAddr(), 
						request.getHeader(HttpHeaders.USER_AGENT));
				
				sessionUtils.addSessionCookies(request, response, session);
				
				return session;
			}
		}
		return null;
	}
	
	private boolean checkRedirects(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
				
			String location = cachedRedirects.get(request.getRequestURI());
			
			if(Objects.nonNull(location)) {
				response.sendRedirect(location);
				return true;
			} else {
				
				log.debug("Checking redirect {}", request.getRequestURI());
				
				
				for(Redirect redirect : redirectDatabase.list(Redirect.class)) {
					Pattern pattern = Pattern.compile(redirect.getPath());
					Matcher matcher = pattern.matcher(request.getRequestURI());
					if(matcher.matches()) {
						
						location = redirect.getLocation();
						for(int i = 0; i <= matcher.groupCount(); i++) { 
							location = location.replace("$" + i, matcher.group(i));
						}
						
						StaticResolver resolver = new StaticResolver();
						resolver.addToken("version", ApplicationVersion.getVersion());
						resolver.addToken("host", request.getHeader(HttpHeaders.HOST));
						resolver.addToken("serverName", request.getServerName());
						
						
						location = ReplacementUtils.processTokenReplacements(location, resolver);
						cachedRedirects.put(request.getRequestURI(), location);
						response.sendRedirect(location);
						return true;
					}
				}
			}
			
		} catch(ObjectNotFoundException e) {
		}
		
		if(request.getRequestURI().equals("/")) {
			response.sendRedirect("/app/ui/");
			return true;
		}
		
	
		return false;
	}
}

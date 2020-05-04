package com.jadaptive.app.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

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
import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.servlet.Request;
import com.jadaptive.api.session.PluginInterceptor;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.app.auth.AuthenticationService;
import com.jadaptive.utils.FileUtils;

@WebFilter(urlPatterns = { "/*" }, dispatcherTypes = DispatcherType.REQUEST)
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
	private UserService userService;  
	
	@Autowired
	private AuthenticationService authenticationService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		
		tenantService.setCurrentTenant(req);
		
		try {

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
			Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
			
			if(Boolean.parseBoolean(properties.getProperty("authentication.allowBasic", "false"))
					&& Objects.nonNull(request.getHeader(HttpHeaders.AUTHORIZATION))) {
				session = performBasicAuthentication(request, response);
			}
			
			if(Objects.isNull(session) && Boolean.parseBoolean(properties.getProperty("authentication.allowAnonymous", "false"))) {
				permissionService.setupSystemContext();
			} else if(Objects.nonNull(session)) {
				tenantService.setCurrentTenant(session.getTenant());	
				permissionService.setupUserContext(userService.getUser(session.getUsername()));
			} 
	
//			String loginURL = properties.getProperty("authentication.loginURL", "/app/ui/login");
//			String requestURL = request.getRequestURI();
//			
//			if(Objects.nonNull(loginURL) && FileUtils.checkEndsWithNoSlash(requestURL).equals(FileUtils.checkEndsWithNoSlash(loginURL))) {
//				return iteratePluginInterceptors(request, response);
//			}
		
			String requireAllPermission = StringUtils.defaultIfBlank(properties.getProperty("permission.requireAll"), null);
			String requireAnyPermission = StringUtils.defaultIfBlank(properties.getProperty("permission.requireAny"), null);
			
//			if(Objects.isNull(session) && "false".equalsIgnoreCase(properties.getProperty("authentication.notRequired", "false"))) {
//				request.getSession().setAttribute(PRE_LOGON_ORIGINAL_URL, requestURL);
//				response.sendRedirect(loginURL);
//				return false;
//			}
			
//			if((Objects.isNull(requireAnyPermission) && Objects.isNull(requireAllPermission)) || Objects.isNull(session)) {
//				return iteratePluginInterceptors(request, response);
//			}
	
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
}

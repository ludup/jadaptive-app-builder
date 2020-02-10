package com.jadaptive.app.auth;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.jadaptive.api.app.SecurityPropertyService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.app.Request;
import com.jadaptive.app.session.SessionUtils;
import com.jadaptive.utils.FileUtils;

@Component
public class SessionInterceptor extends HandlerInterceptorAdapter {

	static Logger log = LoggerFactory.getLogger(SessionInterceptor.class);
	
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
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		Request.setup(request, response);
		
		Session session = sessionUtils.getActiveSession(request);
		Properties properties = securityService.resolveSecurityProperties(request.getRequestURI());
		
		if(Boolean.parseBoolean(properties.getProperty("authentication.allowBasic", "false"))
				&& Objects.nonNull(request.getHeader(HttpHeaders.AUTHORIZATION))) {
			session = performBasicAuthentication(request, response);
		}
		
		if(Objects.isNull(session) && Boolean.parseBoolean(properties.getProperty("authentication.allowAnonymous", "false"))) {
			permissionService.setupSystemContext();
		} else if(Objects.nonNull(session)) {
			tenantService.setCurrentTenant(session.getCurrentTenant());	
			permissionService.setupUserContext(userService.findUsername(session.getUsername()));
		} 

		String loginURL = properties.getProperty("authentication.loginURL");
		String requestURL = request.getRequestURI();
		
		if(Objects.nonNull(loginURL) && FileUtils.checkEndsWithNoSlash(requestURL).equals(FileUtils.checkEndsWithNoSlash(loginURL))) {
			return super.preHandle(request, response, handler);
		}
	
		String requireAllPermission = StringUtils.defaultIfBlank(properties.getProperty("permission.requireAll"), null);
		String requireAnyPermission = StringUtils.defaultIfBlank(properties.getProperty("permission.requireAny"), null);
		
		if((Objects.nonNull(requireAllPermission)
				|| Objects.nonNull(requireAnyPermission))
					&& Objects.isNull(session)) {
			request.getSession().setAttribute(PRE_LOGON_ORIGINAL_URL, requestURL);
			response.sendRedirect(loginURL);
			return false;
		}
		
		if((Objects.isNull(requireAnyPermission) && Objects.isNull(requireAllPermission)) || Objects.isNull(session)) {
			return super.preHandle(request, response, handler);
		}

		if(Objects.nonNull(requireAllPermission)) {
			permissionService.assertAllPermission(requireAllPermission.split(","));
		}
		
		if(Objects.nonNull(requireAnyPermission)) {
			permissionService.assertAnyPermission(requireAnyPermission.split(","));
		}

		return super.preHandle(request, response, handler);
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

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		tenantService.clearCurrentTenant();
		if(permissionService.hasUserContext()) {
			permissionService.clearUserContext();
		}
		Request.tearDown();
		super.postHandle(request, response, handler, modelAndView);
	}

	
}

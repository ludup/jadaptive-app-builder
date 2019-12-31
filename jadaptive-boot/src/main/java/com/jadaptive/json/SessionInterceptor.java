package com.jadaptive.json;

import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.jadaptive.app.SecurityPropertyService;
import com.jadaptive.permissions.PermissionService;
import com.jadaptive.session.Session;
import com.jadaptive.session.SessionUtils;
import com.jadaptive.tenant.TenantService;
import com.jadaptive.user.UserService;
import com.sshtools.common.util.FileUtils;

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
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		Session session = sessionUtils.getActiveSession(request);
			
		if(Objects.nonNull(session)) {
			tenantService.setCurrentTenant(session.getCurrentTenant());	
			permissionService.setupUserContext(userService.findUsername(session.getUsername()));
		} else {
			tenantService.setCurrentTenant(request);
		}
		
		Properties properties = securityService.resolveSecurityProperties(request, request.getRequestURI());
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

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		tenantService.clearCurrentTenant();
		if(permissionService.hasUserContext()) {
			permissionService.clearUserContext();
		}
		super.postHandle(request, response, handler, modelAndView);
	}

	
}

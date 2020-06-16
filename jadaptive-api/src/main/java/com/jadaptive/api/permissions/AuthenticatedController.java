package com.jadaptive.api.permissions;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;

public class AuthenticatedController {

	public static final String SESSION_SCOPE_USER = "com.jadaptive.sessionScopeUser";
	
	static Logger log = LoggerFactory.getLogger(AuthenticatedController.class);
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	protected Tenant getCurrentTenant() {
		return tenantService.getCurrentTenant();
	}
	
	protected void setupUserContext(User user) {
		permissionService.setupUserContext(user);
	}
	
	protected void setupUserContext(HttpServletRequest request) {
		
		User user = sessionUtils.getUser();
		if(Objects.isNull(user)) {
			user = (User) request.getSession().getAttribute(SESSION_SCOPE_USER);
			if(Objects.isNull(user)) {
				Session session = sessionUtils.getActiveSession(request);
				if(Objects.isNull(session)) {
					log.info(request.getMethod() + " " + request.getRequestURI().toString());
					throw new AccessDeniedException();
				}
				user = session.getUser();
			}
		}
		
		permissionService.setupUserContext(user);
	}
	
	protected void setupSystemContext() {
		permissionService.setupSystemContext();
	}
	
	protected void clearUserContext() {
		permissionService.clearUserContext();
	}
	
	protected User getCurrentUser() {
		return permissionService.getCurrentUser();
	}
	
	protected boolean hasUserContext() {
		return permissionService.hasUserContext();
	}
	
	protected void assertRead(String resourceKey) {
		permissionService.assertRead(resourceKey);
	}
	
	protected void assertWrite(String resourceKey) {
		permissionService.assertReadWrite(resourceKey);
	}
	
	protected void assertPermission(String permission) {
		permissionService.assertPermission(permission);
	}
	
	protected void assertAnyPermissions(String... permissions) {
		permissionService.assertAnyPermission(permissions);
	}
	
	protected void assertAllPermissions(String... permissions) {
		permissionService.assertAllPermission(permissions);
	}
	
	protected boolean isValidPermission(String permission) {
		return permissionService.isValidPermission(permission);
	}
}

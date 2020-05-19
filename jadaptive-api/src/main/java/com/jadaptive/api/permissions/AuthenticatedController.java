package com.jadaptive.api.permissions;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionUtils;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

public class AuthenticatedController {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionUtils sessionUtils;
	
	@Autowired
	private UserService userService; 
	
	protected Tenant getCurrentTenant() {
		return tenantService.getCurrentTenant();
	}
	
	protected void setupUserContext(User user) {
		permissionService.setupUserContext(user);
	}
	
	protected void setupUserContext(HttpServletRequest request) {
		Session session = sessionUtils.getActiveSession(request);
		if(Objects.isNull(session)) {
			throw new AccessDeniedException();
		}
		
		permissionService.setupUserContext(userService.getUser(session.getUsername()));
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

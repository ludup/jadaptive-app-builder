package com.jadaptive.api.permissions;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.user.User;

public class AuthenticatedService {

	@Autowired
	private PermissionService permissionService; 
						
	protected void setupUserContext(User user) {
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
}

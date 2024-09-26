package com.jadaptive.api.permissions;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;

public class AuthenticatedService {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	protected TenantService tenantService; 
	
	@Autowired
	private TemplateService templateService;
	
	protected Tenant getCurrentTenant() {
		return tenantService.getCurrentTenant();
	}
	
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
	
	
	protected boolean isAdministrator(User currentUser) {
		return permissionService.isAdministrator(currentUser);
	}
	
	protected void assertRead(String resourceKey) {
		assertRead(templateService.get(resourceKey));
	}
	
	protected void assertRead(ObjectTemplate template) {
		if(template.getPermissionProtected()) {
			if(template.hasParent()) {
				permissionService.assertRead(templateService.getParentTemplate(template).getResourceKey());
			} else {
				permissionService.assertRead(template.getResourceKey());
			}
		}
	}
	
	protected void assertWrite(String resourceKey) {
		assertWrite(templateService.get(resourceKey));
	}
	
	protected void assertWrite(ObjectTemplate template) {
		if(template.getPermissionProtected()) {
			if(template.hasParent()) {
				permissionService.assertWrite(templateService.getParentTemplate(template).getResourceKey());
			} else {
				permissionService.assertWrite(template.getResourceKey());
			}
		}
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

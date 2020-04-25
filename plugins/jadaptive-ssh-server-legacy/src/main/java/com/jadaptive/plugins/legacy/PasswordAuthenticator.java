package com.jadaptive.plugins.legacy;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.maverick.sshd.Connection;
import com.maverick.sshd.PasswordAuthenticationProvider;
import com.maverick.sshd.platform.PasswordChangeException;

@Component
public class PasswordAuthenticator extends PasswordAuthenticationProvider {

	static Logger log = LoggerFactory.getLogger(PasswordAuthenticator.class);
	
	@Autowired
	private UserService userService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public boolean verifyPassword(Connection con, String username, String password)
			throws PasswordChangeException, IOException {
		
		tenantService.setCurrentTenant(StringUtils.substringAfter(username, "@"));
		
		try {
			
			User user = userService.getUser(username);
			permissionService.setupUserContext(user);
			
			try {
				boolean success = userService.verifyPassword(user, password.toCharArray());
				
				boolean adminPermitted = ApplicationProperties.getValue("sshd.permitAdminPassword", true);
				
				if(permissionService.isAdministrator(user) && !adminPermitted) {
					if(log.isWarnEnabled()) {
						log.warn("Administrator denied login using password due to sshd.permitAdminPassword setting");
					}
					return false;
				}
				
				if(success && user.getPasswordChangeRequired()) {
					try {
						permissionService.assertAnyPermission( 
								UserService.CHANGE_PASSWORD_PERMISSION,
								UserService.SET_PASSWORD_PERMISSION);
						throw new PasswordChangeException();
					} catch(AccessDeniedException e) {
					}
				}
				if(success) {
					con.setProperty(LegacySSHServer.TENANT, tenantService.getCurrentTenant());
					con.setProperty(LegacySSHServer.USER, user);
				}
				return success;
			} finally {
				permissionService.clearUserContext();
			}
		} catch(EntityException e) {
			return false;
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

	@Override
	public boolean changePassword(Connection con, String username, String oldpassword, String newpassword)
			throws PasswordChangeException, IOException {
		
		tenantService.setCurrentTenant(StringUtils.substringAfter(username, "@"));

		try {
			
			User user = userService.getUser(username);
			permissionService.setupUserContext(user);

			try {
				userService.changePassword(user,
					oldpassword.toCharArray(), newpassword.toCharArray());
			
				con.setProperty(LegacySSHServer.TENANT, tenantService.getCurrentTenant());
				con.setProperty(LegacySSHServer.USER, user);
			} finally {
				permissionService.clearUserContext();
			}

			return true;
		} catch(EntityException e) {
			return false;
		} catch(AccessDeniedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			tenantService.clearCurrentTenant();
		}
		
	}
}

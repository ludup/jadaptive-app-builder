package com.jadaptive.plugins.sshd;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.auth.PasswordAuthenticationProvider;
import com.sshtools.common.auth.PasswordChangeException;
import com.sshtools.common.logger.Log;
import com.sshtools.common.ssh.SshConnection;

@Component
public class PasswordAuthenticatorImpl extends PasswordAuthenticationProvider {

	@Autowired
	private UserService userService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public boolean verifyPassword(SshConnection con, String username, String password)
			throws PasswordChangeException, IOException {
		
		tenantService.setCurrentTenant(StringUtils.substringAfter(username, "@"));
		
		try {
			
			User user = userService.getUser(username);
			permissionService.setupUserContext(user);
			
			try {
				boolean success = userService.verifyPassword(user, password.toCharArray());
				
				boolean adminPermitted = ApplicationProperties.getValue("sshd.permitAdminPassword", true);
				
				if(permissionService.isAdministrator(user) && !adminPermitted) {
					if(Log.isWarnEnabled()) {
						Log.warn("Administrator denied login using password due to sshd.permitAdminPassword setting");
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
					con.setProperty(SSHDService.TENANT, tenantService.getCurrentTenant());
					con.setProperty(SSHDService.USER, user);
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
	public boolean changePassword(SshConnection con, String username, String oldpassword, String newpassword)
			throws PasswordChangeException, IOException {
		
		tenantService.setCurrentTenant(StringUtils.substringAfter(username, "@"));

		try {
			
			User user = userService.getUser(username);
			permissionService.setupUserContext(user);

			try {
				userService.changePassword(user,
					oldpassword.toCharArray(), newpassword.toCharArray());
			
				con.setProperty(SSHDService.TENANT, tenantService.getCurrentTenant());
				con.setProperty(SSHDService.USER, user);
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

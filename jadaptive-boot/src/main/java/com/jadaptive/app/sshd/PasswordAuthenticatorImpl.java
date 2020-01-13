package com.jadaptive.app.sshd;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.sshd.SSHDService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.auth.PasswordAuthenticationProvider;
import com.sshtools.common.auth.PasswordChangeException;
import com.sshtools.common.ssh.SshConnection;

@Component
public class PasswordAuthenticatorImpl extends PasswordAuthenticationProvider {

	@Autowired
	UserService userService; 
	
	@Autowired
	TenantService tenantService; 
	
	@Override
	public boolean verifyPassword(SshConnection con, String username, String password)
			throws PasswordChangeException, IOException {
		
		tenantService.setCurrentTenant(StringUtils.substringAfter(username, "@"));
		
		try {
			
			User user = userService.findUsername(username);
			boolean success = userService.verifyPassword(user, password.toCharArray());
			if(success) {
				con.setProperty(SSHDService.TENANT, tenantService.getCurrentTenant());
				con.setProperty(SSHDService.USER, user);
			}
			return success;
		} catch(EntityException e) {
			return false;
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

	@Override
	public boolean changePassword(SshConnection con, String username, String oldpassword, String newpassword)
			throws PasswordChangeException, IOException {
		return false;
	}

}

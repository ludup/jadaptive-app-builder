package com.jadaptive.sshd;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.entity.EntityException;
import com.jadaptive.tenant.TenantService;
import com.jadaptive.user.User;
import com.jadaptive.user.UserService;
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

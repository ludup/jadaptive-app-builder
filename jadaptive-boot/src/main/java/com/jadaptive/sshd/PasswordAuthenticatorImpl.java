package com.jadaptive.sshd;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.entity.EntityException;
import com.jadaptive.user.User;
import com.jadaptive.user.UserService;
import com.sshtools.common.auth.PasswordAuthenticationProvider;
import com.sshtools.common.auth.PasswordChangeException;
import com.sshtools.common.ssh.SshConnection;

@Component
public class PasswordAuthenticatorImpl extends PasswordAuthenticationProvider {

	@Autowired
	UserService userService; 
	
	@Override
	public boolean verifyPassword(SshConnection con, String username, String password)
			throws PasswordChangeException, IOException {
		try {
			User user = userService.findUsername(username);
			return userService.verifyPassword(user, password.toCharArray());
		} catch(EntityException e) {
			return false;
		}
	}

	@Override
	public boolean changePassword(SshConnection con, String username, String oldpassword, String newpassword)
			throws PasswordChangeException, IOException {
		return false;
	}

}

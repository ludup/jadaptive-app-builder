package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

public abstract class UserCommand extends AbstractTenantAwareCommand {

	@Autowired
	private PermissionService permissionService;
	
	public UserCommand(String name, String subsystem, String signature, String description) {
		super(name, subsystem, signature, description);
	}

	protected User resolveUser(String name) {
		return userService.getUser(name);
	}
	
	protected void assertPermission() {
		permissionService.assertReadWrite(UserService.USER_RESOURCE_KEY);
	}
	
	protected User getCommandLineUser(boolean requirePassword) throws IOException {
		
		if(args.length > 1) {
			assertPermission();
			return userService.getUser(args[args.length-1]);
		} else if(requirePassword){

			char[] currentPassword = promptForPassword("Current Password: ");

			if(!userService.verifyPassword(currentUser, currentPassword)) {
				throw new IOException("Bad password");
			}
		}
		
		return currentUser;
	}

	protected boolean isCurrentUser(User user) {
		return user.getUsername().equals(console.getEnvironment().get("USER"));
	}
}

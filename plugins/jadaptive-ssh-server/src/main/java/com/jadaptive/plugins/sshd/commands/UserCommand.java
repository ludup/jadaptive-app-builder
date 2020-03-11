package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;

import com.jadaptive.api.user.User;

public abstract class UserCommand extends AbstractTenantAwareCommand {

	public UserCommand(String name, String subsystem, String signature, String description) {
		super(name, subsystem, signature, description);
	}

	protected User resolveUser(String name) {
		return userService.getUser(name);
	}
	
	protected User verifyUser(boolean requirePassword) throws IOException {
		
		User user = userService.getUser(console.getConnection().getUsername());
		
		if(args.length > 1) {
			
			assertAdministrationPermission();
			
			user = userService.getUser(args[1]);
		} else if(requirePassword){
			
			
			char[] currentPassword = promptForPassword("Current Password: ");

			if(!userService.verifyPassword(user, currentPassword)) {
				throw new IOException("Bad password");
			}
		}
		
		return user;
	}

	protected boolean isCurrentUser(User user) {
		return user.getUsername().equals(console.getEnvironment().get("USER"));
	}
}

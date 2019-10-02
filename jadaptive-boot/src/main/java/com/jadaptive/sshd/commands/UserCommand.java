package com.jadaptive.sshd.commands;

import com.jadaptive.user.User;

public abstract class UserCommand extends AbstractCommand {

	public UserCommand(String name, String subsystem, String signature, String description) {
		super(name, subsystem, signature, description);
	}

	protected User verifyUser(boolean requirePassword) {
		
		User user = userService.findUsername(console.getConnection().getUsername());
		
		if(args.length > 1) {
			
			assertAdministrationPermission();
			
			user = userService.findUsername(args[1]);
		} else if(requirePassword){
			
			
			char[] currentPassword = promptForPassword("Current Password: ");

			userService.verifyPassword(user, currentPassword);
		}
		
		return user;
	}

}

package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Passwd extends UserCommand {

	@Autowired
	UserService userService; 
	
	public Passwd() {
		super("passwd", 
				"User Management",
				UsageHelper.build("passwd [options] <user>",
						"-f, --forceChange		       Force the user to change password at next logon"),
						"Change the current user or another users password.");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		
		User user = getCommandLineUser(true);
	
		for(int i=0;i<3;i++) {
			char[] newPassword = promptForPassword("New Password: ");
			char[] confirmedPassword = promptForPassword("Confirm Password: ");
			
			if(!Arrays.equals(newPassword, confirmedPassword)) {
				console.println("Passwords do not match!");
				continue;
			}
			
			try {
				if(isCurrentUser(user)) {
					userService.changePassword(user, newPassword, false);
					console.println("Your password was changed!");
				} else {
					boolean passwordChangeRequired = CliHelper.hasOption(args, 'f', "forceChange");
					userService.setPassword(user, newPassword, passwordChangeRequired);
					console.println(String.format("Password for %s changed!", user.getUsername()));
				}
				
				break;
			} catch(EntityException e) { 
				continue;
			}
		}

	}





}

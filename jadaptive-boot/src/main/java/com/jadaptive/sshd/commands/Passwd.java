package com.jadaptive.sshd.commands;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.entity.EntityException;
import com.jadaptive.user.User;
import com.jadaptive.user.UserService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Passwd extends UserCommand {

	@Autowired
	UserService userService; 
	
	public Passwd() {
		super("passwd", "User", "passwd <user>", "Change the current user or another users password.");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		
		User user = verifyUser(true);
	
		for(int i=0;i<3;i++) {
			char[] newPassword = promptForPassword("New Password: ");
			char[] confirmedPassword = promptForPassword("Confirm Password: ");
			
			if(!Arrays.equals(newPassword, confirmedPassword)) {
				console.println("Passwords do not match!");
				continue;
			}
			
			try {
				userService.setPassword(user, newPassword);
				console.println("Password changed!");
				break;
			} catch(EntityException e) { 
				continue;
			}
		}

	}





}

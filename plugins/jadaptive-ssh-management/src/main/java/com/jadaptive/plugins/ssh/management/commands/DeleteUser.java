package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.user.BuiltinUserDatabase;
import com.jadaptive.api.user.User;
import com.jadaptive.plugins.sshd.commands.UserCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class DeleteUser extends UserCommand {
	
	@Autowired
	private BuiltinUserDatabase userService; 
	
	public DeleteUser() {
		super("delete-user", "User Management", "delete-user [<username>",
				"Delete a builtin user account");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		User user = getCommandLineUser(false);
		
		if(isCurrentUser(user)) {
			throw new PermissionDeniedException("You cannot delete your own account!");
		}
		String username = console.readLine("Please confirm you want to delete the account by entering the username again: ");
		User confirmedUser = userService.getUser(username);
		
		if(!username.equals(user.getUsername())) {
			throw new PermissionDeniedException("Confirmation user does not match command line argument");
		}
		
		userService.deleteUser(confirmedUser);
		
		console.println(String.format("The user %s has been deleted", confirmedUser.getUsername()));
	}
}

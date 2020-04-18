package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.UserCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class UpdateUser extends UserCommand {
	
	@Autowired
	private UserService userService;  
	
	public UpdateUser() {
		super("update-user", "User Management", UsageHelper.build("update-user [options] <username>",
				"-e, --email <email>    Change the email address of the account",
				"-n, --name  <name>     Change the full name of the account"),
				"Update a builtin user account");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
	
		User user = getCommandLineUser(false);
		
		if(CliHelper.hasOption(args, 'e', "email")) {
			user.setEmail(CliHelper.getValue(args, 'e', "email"));
		}
		
		if(CliHelper.hasOption(args, 'n', "name")) {
			user.setName(CliHelper.getValue(args, 'n', "name"));
		}
		
		userService.updateUser(user);
		
		console.println(String.format("Updated user %s", user.getUsername()));
	}
}

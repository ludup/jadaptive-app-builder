package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Users extends UserCommand {

	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	UserService userService; 
	
	public Users() {
		super("users", "User Management", UsageHelper.build("users [option] [user]",
				"-l, --list          List all users"),
				"List all users");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} else if(args.length==1 || CliHelper.hasShortOption(args, 'l') || CliHelper.hasLongOption(args, "list")) {	
			printUsers();
		} else {
			console.println("Invalid arguments!");
			printUsage();
		}
	}

	private void printUsers() {
		for(User user : userService.iterateUsers()) {
			console.println(user.getUsername());
		}
	}
}

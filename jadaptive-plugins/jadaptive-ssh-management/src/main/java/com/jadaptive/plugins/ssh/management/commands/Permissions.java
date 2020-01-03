package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Permissions extends UserCommand {

	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	UserService userService; 
	
	public Permissions() {
		super("permissions", "User Management", UsageHelper.build("permissions [option] [user]",
				"-l, --list                           List all permissions"),
				"List all permissions");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} else if(args.length==1 || CliHelper.hasShortOption(args, 'l') || CliHelper.hasLongOption(args, "list")) {	
			printPermissions();
		} else {
			console.println("Invalid arguments!");
			printUsage();
		}
	}

	private void printPermissions() {
		for(String perm : permissionService.getAllPermissions()) {
			console.println(perm);
		}
	}
}

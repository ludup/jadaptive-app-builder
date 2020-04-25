package com.jadaptive.plugins.ssh.management.commands.users;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Users extends AbstractTenantAwareCommand {
	
	@Autowired
	private UserService userService;
	
	public Users() {
		super("users", "User Management", "users", "List all users");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		printUsers();
	}

	private void printUsers() {
		for(User user : userService.iterateUsers()) {
			console.println(user.getUsername());
		}
	}
}

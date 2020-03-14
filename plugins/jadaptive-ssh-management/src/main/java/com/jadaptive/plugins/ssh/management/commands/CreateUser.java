package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.BuiltinUserDatabase;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class CreateUser extends AbstractTenantAwareCommand {
	
	@Autowired
	private BuiltinUserDatabase userService; 
	
	@Autowired
	private PermissionService permissionService;
	
	public CreateUser() {
		super("create-user", "User Management", "create-user", "Create a builtin user account");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		createUser();
	}

	private void createUser() {
		
		permissionService.assertReadWrite(UserService.USER_RESOURCE_KEY);
		
		String username = console.readLine("Username: ");
		String fullname = console.readLine("Full Name: ");
		String email = console.readLine("Email Address: ");
		
		String password;
		String confirmPassword;
		boolean identical;
		do {
			password = console.getLineReader().readLine("Password: ", '*');
			confirmPassword = console.getLineReader().readLine("Confirm Password: ", '*');
			identical = StringUtils.equals(password, confirmPassword);
			if(!identical) {
				console.println("Passwords do not match");
			}
		} while(!identical);
		
		userService.createUser(username, fullname, email, password.toCharArray(), false);
		
		console.println(String.format("Created user %s", username));
	}
}

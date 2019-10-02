package com.jadaptive.sshd.commands;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.role.Role;
import com.jadaptive.role.RoleService;
import com.jadaptive.user.User;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Roles extends UserCommand {

	@Autowired
	RoleService roleService; 
	
	public Roles() {
		super("roles", "Users", "roles <user>", "List the roles of a user");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		
		User user = verifyUser(false);
		
		Collection<Role> roles = roleService.getRoles(user);

		for(Role role : roles) {
			console.println(role.getName());
		}
	}

}

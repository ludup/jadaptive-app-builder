package com.jadaptive.plugins.ssh.management.commands;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.sshd.PluginCommandFactory;
import com.jadaptive.api.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class TenantUserCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	@Autowired
	PermissionService permissionService; 
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		tryCommand("roles", Roles.class, "roles.read", "roles.readWrite");
		tryCommand("permissions", Permissions.class, "role.read", "roles.readWrite");
		tryCommand("users", Users.class, "user.read", "user.readWrite");

		return this;
	}

	private void tryCommand(String name, Class<? extends ShellCommand> clz, String... permissions) {
		try {
			permissionService.assertAnyPermission(permissions);
			installCommand(name, clz);
		} catch(AccessDeniedException e) { }
	}
	
}
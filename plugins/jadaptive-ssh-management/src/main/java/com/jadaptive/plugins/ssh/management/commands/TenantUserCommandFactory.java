package com.jadaptive.plugins.ssh.management.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Component
public class TenantUserCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	@Autowired
	PermissionService permissionService; 
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		tryCommand("roles", Roles.class, "roles.read", "roles.readWrite");
		tryCommand("permissions", Permissions.class, "role.read", "roles.readWrite");
		tryCommand("users", Users.class, "user.read", "user.readWrite");
		tryCommand("templates", Templates.class, "entityTemplate.read", "entityTemplate.readWrite");
		tryCommand("security", Security.class, "tenant.read", "tenant.readWrite");

		return this;
	}

	private void tryCommand(String name, Class<? extends ShellCommand> clz, String... permissions) {
		try {
			permissionService.assertAnyPermission(permissions);
			installCommand(name, clz);
		} catch(AccessDeniedException e) { }
	}
	
}
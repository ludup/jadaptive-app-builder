package com.jadaptive.plugins.ssh.management.commands.roles;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class RoleCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	static Logger log = LoggerFactory.getLogger(RoleCommandFactory.class);
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		
		tryCommand("roles", Roles.class, RoleService.READ_PERMISSION);
		tryCommand("create-role", CreateRole.class, RoleService.READ_WRITE_PERMISSION);
		tryCommand("delete-role", DeleteRole.class, RoleService.READ_WRITE_PERMISSION);
		tryCommand("assign-role", AssignRole.class, RoleService.READ_WRITE_PERMISSION);
		tryCommand("unassign-role", UnassignRole.class, RoleService.READ_WRITE_PERMISSION);
		
		tryCommand("permissions", Permissions.class, RoleService.READ_PERMISSION);
		tryCommand("grant-permission", GrantPermission.class, RoleService.READ_WRITE_PERMISSION);
		tryCommand("revoke-permission", RevokePermission.class, RoleService.READ_WRITE_PERMISSION);
		
		return this;
	}
	
}
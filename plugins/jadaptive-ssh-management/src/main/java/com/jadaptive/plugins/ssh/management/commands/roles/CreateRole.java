package com.jadaptive.plugins.ssh.management.commands.roles;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class CreateRole extends AbstractTenantAwareCommand {

	@Autowired
	private RoleService roleService; 
	
	public CreateRole() {
		super("create-role", "Role Management", UsageHelper.build("create-role <name>"), "Create a Role");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length != 2) {
			printUsage();
		} else {
			
			String roleName = args[1];
			
			try {
				resolveRole(roleName);
				console.println(String.format("There is already a Role named %s", roleName));
			} catch(ObjectNotFoundException e) {
				Role role = roleService.createRole(roleName);
				console.println(String.format("Role %s created", role.getName()));
			}
		} 
	}
	
	private Role resolveRole(String name) {
		return roleService.getRoleByName(name);
	}

}

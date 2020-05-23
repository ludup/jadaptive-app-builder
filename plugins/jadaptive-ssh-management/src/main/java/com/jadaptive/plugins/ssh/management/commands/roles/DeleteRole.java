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

public class DeleteRole extends AbstractTenantAwareCommand {

	@Autowired
	private RoleService roleService;  
	
	public DeleteRole() {
		super("delete-role", "Role Management", UsageHelper.build("create-role [role]"), "Delete a Role");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length != 2) {
			printUsage();
		} else {
			
			String roleName = args[1];
			
			try {
				;
				roleService.deleteRole(resolveRole(roleName));
				console.println(String.format("Role %s deleted", roleName));
				
			} catch(ObjectNotFoundException e) {
				console.println(String.format("There is no Role named %s", roleName));
			}
		} 
	}
	
	private Role resolveRole(String name) {
		return roleService.getRoleByName(name);
	}

}

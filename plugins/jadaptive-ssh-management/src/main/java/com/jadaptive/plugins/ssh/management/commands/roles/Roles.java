package com.jadaptive.plugins.ssh.management.commands.roles;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Roles extends AbstractTenantAwareCommand {

	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private UserService userService; 
	
	public Roles() {
		super("roles", "Role Management", UsageHelper.build("roles [option]",
				"-l, --list                           List roles",
				"-m, --members <name>                 List the members of a role",
				"-p, --permissions <name>             List the permissions of a role"), 
					"Output information about roles");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} else if(args.length==1 || CliHelper.hasShortOption(args, 'l') || CliHelper.hasLongOption(args, "list")) {	
			printRoles(roleService.listRoles());
		} else if(args.length==3 &&  (CliHelper.hasShortOption(args, 'm') || CliHelper.hasLongOption(args, "members"))) {
			printMembers(resolveRole(args[2]));
		} else if(args.length==3 &&  (CliHelper.hasShortOption(args, 'p') || CliHelper.hasLongOption(args, "permissions"))) {
			printPermissions(resolveRole(args[2]));
		} else {
			console.println("Invalid arguments!");
			printUsage();
		}
	}

	private Role resolveRole(String name) {
		return roleService.getRoleByName(name);
	}

	private void printPermissions(Role role) {
		if(role.isAllPermissions()) {
			console.println(String.format("%s contains all available permissions", role.getName()));
		} else {
			if(role.getPermissions().isEmpty()) {
				console.println(String.format("%s does not contain any permissions", role.getName()));

			} else {
				for(String permission : role.getPermissions()) {
					console.println(permission);
				}
			}
		}
	}
	
	private void printRoles(Collection<Role> roles) {
		for(Role role : roles) {
			console.println(role.getName());
		}
	}
	
	private void printMembers(Role role) {
		if(role.isAllUsers()) {
			console.println(String.format("All users are assigned to the %s Role", role.getName()));
		} else {
			for(String uuid : role.getUsers()) {
				console.println(userService.getUserByUUID(uuid).getUsername());
			}
		}
	}
		

}

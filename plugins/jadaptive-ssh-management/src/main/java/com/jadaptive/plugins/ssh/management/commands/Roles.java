package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.UserCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Roles extends UserCommand {

	@Autowired
	RoleService roleService; 
	
	@Autowired
	UserService userService; 
	
	public Roles() {
		super("roles", "User Management", UsageHelper.build("roles [option] [role] [user...]",
				"-l, --list                           List roles",
				"-c, --create <name>                  Create a role",
				"-d, --delete <name>                  Delete a role",
				"-a, --assign <name> <usernames...>   Assign a role to users",
				"-u, --unassign <name> <usernames...> Unassign users from a role",
				"-m, --members <name>                 List the members of a role",
				"-p, --permissions <name>             List the permissions of a role",
				"-g, --grant <name> <permissions...>  Grant one or more permissions to a role",
				"-r, --revoke <name> <permissions...> Revoke one or more permissions from a role")
				, "Manage roles");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} else if(args.length==1 || CliHelper.hasShortOption(args, 'l') || CliHelper.hasLongOption(args, "list")) {	
			printRoles(roleService.list());
		} else if(args.length==3 &&  (CliHelper.hasShortOption(args, 'm') || CliHelper.hasLongOption(args, "members"))) {
			printMembers(resolveRole(args[2]));
		} else if(CliHelper.hasShortOption(args, 'c') || CliHelper.hasLongOption(args, "create")) {
			
			String roleName = args[2];
			
			try {
				resolveRole(roleName);
				console.println(String.format("There is already a Role named %s", roleName));
			} catch(EntityNotFoundException e) {
				Role role = roleService.createRole(roleName, resolveUsers());
				console.println(String.format("Role %s created", role.getName()));
			}
		} else if(args.length >= 3) {
			
			Role role = resolveRole(args[2]);

			switch(args[1]) {
			case "-a":
			case "--assign":
			{
				for(User user : resolveUsers()) {
					roleService.assignRole(role, user);
					console.println(String.format("%s was assigned to %s", user.getUsername(), role.getName()));
				}
				break;
			}
			case "-u":
			case "--unassign":
			{
				for(User user : resolveUsers()) {
					roleService.unassignRole(role, user);
					console.println(String.format("%s was unassigned from %s", user.getUsername(), role.getName()));
				}
				break;
			}
			case "-d":
			case "--delete":
			{
				roleService.delete(role);
				console.println(String.format("%s was deleted", role.getName()));
				break;
			}
			case "-p":
			case "--permissions":
			{
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
				break;
			}
			case "-g":
			case "--grant":
			{
				roleService.grantPermission(role, resolvePermissions());
				console.println(String.format("Permissions were granted to role %s", role.getName()));
				break;
			}
			case "-r":
			case "--revoke":
			{
				roleService.revokePermission(role, resolvePermissions());
				console.println(String.format("Permissions were revoked from role %s", role.getName()));
				break;
			}
			default:
				console.println("Invalid arguments!");
				printUsage();
				break;
			}
		} else {
			console.println("Invalid arguments!");
			printUsage();
		}
	}
	
	private String[] resolvePermissions() {
		Set<String> permissions = new HashSet<>();
		int argIndex = 3;
		while(args.length > argIndex) {
			permissions.add(args[argIndex++]);
		}
		return permissions.toArray(new String[0]);
	}

	private Collection<User> resolveUsers() {
		Set<User> users = new HashSet<>();
		int argIndex = 3;
		while(args.length > argIndex) {
			users.add(resolveUser(args[argIndex++]));
		}
		return users;
	}

	private Role resolveRole(String name) {
		return roleService.getRoleByName(name);
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

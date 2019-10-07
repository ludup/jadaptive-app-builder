package com.jadaptive.sshd.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.role.Role;
import com.jadaptive.role.RoleService;
import com.jadaptive.user.User;
import com.jadaptive.user.UserService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Roles extends UserCommand {

	@Autowired
	RoleService roleService; 
	
	@Autowired
	UserService userService; 
	
	public Roles() {
		super("roles", "User Management", "roles [options] [role] [user...]", "Manage roles");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(CliHelper.hasLongOption(args, "help")) {
			printUsage();
		} else if(args.length==1) {	
			printRoles(roleService.list());
		} else if(args.length==2 && !args[1].startsWith("-")) {
			printMembers(resolveRole(args[1]));
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
			default:
				console.println("Invalid arguments!");
				printUsage();
				break;
			}
		}
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
				console.println(userService.getUser(uuid).getUsername());
			}
		}
	}
		

}

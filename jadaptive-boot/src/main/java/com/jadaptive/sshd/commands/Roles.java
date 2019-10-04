package com.jadaptive.sshd.commands;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

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
		super("roles", "Users", "roles [options] user role", "List the roles of a user");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		
		if(args.length==1) {	
			printRoles(roleService.list());
		} else if(args.length==2 && !args[1].startsWith("-")) {
			printRoles(roleService.getRoles(verifyUser(false)));
		} else if(args.length >= 3) {
			switch(args[1]) {
			case "-a":
			case "--assign":
			{
				if(checkAssignmentArguments()) {
					Role role = resolveRole(args[3]);
					User user = resolveUser(args[2]);
					if(checkValidArgumments(role, user)) {
						roleService.assignRole(role, user);
						console.println(String.format("%s was assigned to %s", user.getUsername(), role.getName()));
					}
				}
				break;
			}
			case "-u":
			case "--unassign":
			{
				if(checkAssignmentArguments()) {
					Role role = resolveRole(args[3]);
					User user = resolveUser(args[2]);
					if(checkValidArgumments(role, user)) {
						roleService.unassignRole(role, user);
						console.println(String.format("%s was unassigned from %s", user.getUsername(), role.getName()));
					}
				}
				break;
			}
			case "-c":
			case "--create":
			{
				
				break;
			}
			case "-d":
			case "--delete":
			{
				
				break;
			}
			default:
				console.println("Invalid arguments!");
				printUsage();
				break;
			}
		}
	}

	private boolean checkValidArgumments(Role role, User user) {
		if(Objects.isNull(role)) {
			console.println("Invalid role name!");
		}
		if(Objects.isNull(user)) {
			console.println("Invalid user name!");
		}
		return Objects.nonNull(role) && Objects.nonNull(user);
	}

	private boolean checkAssignmentArguments() {
		if(args.length < 4) {
			console.println("Missing user or role argument!");
			return false;
		}
		return true;
	}
	
	private Role resolveRole(String name) {
		return roleService.getRoleByName(name);
	}

	private void printRoles(Collection<Role> roles) {
		for(Role role : roles) {
			console.println(role.getName());
		}
	}

}

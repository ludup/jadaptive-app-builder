package com.jadaptive.plugins.ssh.management.commands.roles;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class UnassignRole extends AbstractTenantAwareCommand {

	@Autowired
	RoleService roleService; 
	
	@Autowired
	UserService userService; 
	
	public UnassignRole() {
		super("unassign-role", "Role Management", UsageHelper.build("unassign-role [role] user1 user2..."), "Unassign a Role");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length < 3) {
			printUsage();
		} else {
			
			String roleName = args[1];
			
			try {
				Role role = resolveRole(roleName);
				
				for(User user : resolveUsers()) {
					roleService.unassignRole(role, user);
					console.println(String.format("%s was assigned to %s", user.getUsername(), role.getName()));
				}
				
			} catch(EntityNotFoundException e) {
				console.println(String.format("There is no Role named %s", roleName));
			}
		} 
	}
	
	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		if(line.wordIndex() == 1) {
			// Roles
			for(Role role : roleService.listRoles()) {
				candidates.add(new Candidate(role.getName()));
			}
		} else if(line.wordIndex() >= 2) {
			// Users
			Role role = roleService.getRoleByName(line.words().get(1));
			for(String user : role.getUsers()) {
				candidates.add(new Candidate(userService.getUserByUUID(user).getUsername()));
			}
		}
	}
	
	private Collection<User> resolveUsers() {
		Set<User> users = new HashSet<>();
		int argIndex = 2;
		while(args.length > argIndex) {
			users.add(userService.getUser(args[argIndex++]));
		}
		return users;
	}
	
	private Role resolveRole(String name) {
		return roleService.getRoleByName(name);
	}

}

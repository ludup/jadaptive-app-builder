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

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class RevokePermission extends AbstractTenantAwareCommand {

	@Autowired
	private RoleService roleService;  
	
	public RevokePermission() {
		super("revoke-permission", "Role Management", UsageHelper.build("revoke-permission [role] permission1 permission2..."), "Revoke a Permission from a Role");
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
				
				for(String permission : resolvePermissions()) {
					roleService.revokePermission(role, permission);
					console.println(String.format("%s was revoked to %s", permission, role.getName()));
				}
				
			} catch(ObjectNotFoundException e) {
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
			// Permissions
			Role role = roleService.getRoleByName(line.words().get(1));
			for(String permission : role.getPermissions()) {
				candidates.add(new Candidate(permission));
			}
		}
	}
	
	private Collection<String> resolvePermissions() {
		Set<String> permissions = new HashSet<>();
		int argIndex = 2;
		while(args.length > argIndex) {
			permissions.add(args[argIndex++]);
		}
		return permissions;
	}
	
	private Role resolveRole(String name) {
		return roleService.getRoleByName(name);
	}

}

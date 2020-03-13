package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.AuthorizedKey;
import com.jadaptive.plugins.sshd.AuthorizedKeyService;
import com.jadaptive.plugins.sshd.AuthorizedKeyServiceImpl;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class DeleteKey extends AbstractTenantAwareCommand {

	@Autowired
	UserService userService; 
	
	@Autowired
	AuthorizedKeyService authorizedKeyService; 
	
	@Autowired
	PermissionService permissionService; 
	
	public DeleteKey() {
		super("delete-key", 
				"User",
				UsageHelper.build("delete-key -a [user] [name]",
						"-a, --assign      Delete a key assigned to another user (requires administrative or authorizedKey.assign permission",
						"-n, --name        The name of the key you want to delete"),
						"Delete SSH keys");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		boolean assign = CliHelper.hasOption(args, 'a', "assign");
		User forUser = currentUser;
		if(assign) {
			try {
				forUser = userService.getUser(CliHelper.getValue(args, 'a', "assign"));
			} catch(EntityNotFoundException e) {
				throw new IOException(e.getMessage(), e);
			}
			try {
				permissionService.assertAnyPermission(AuthorizedKeyServiceImpl.AUTHORIZED_KEY_ASSIGN);
			} catch(AccessDeniedException e) {
				throw new IOException("You do not have the permission to delete a key for " + forUser.getUsername());
			}
		}
		
		if(!CliHelper.hasOption(args, 'n', "name")) {
			throw new IOException("-n or --name option required to delete key");
		}
		
		String name = CliHelper.getValue(args, 'n', "name");
		
		AuthorizedKey key = authorizedKeyService.getAuthorizedKey(forUser, name);
		authorizedKeyService.deleteKey(key);

	}
}
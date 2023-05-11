package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.keys.AuthorizedKey;
import com.jadaptive.plugins.keys.AuthorizedKeyService;
import com.jadaptive.plugins.keys.AuthorizedKeyServiceImpl;
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
				"Key Management",
				UsageHelper.build("delete-key <name>"),
						"Delete an authorized key");
	}

	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		if(line.wordIndex() == 1) {
			for(AuthorizedKey key : authorizedKeyService.getAuthorizedKeys()) {
				candidates.add(new Candidate(key.getName()));
			}
		} 
	}
	
	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		if(args.length < 2) {
			throw new UsageException("Missing key name");
		}
		
		boolean assign = CliHelper.hasOption(args, 'a', "assign");
		User forUser = currentUser;
		if(assign) {
			try {
				forUser = userService.getUser(CliHelper.getValue(args, 'a', "assign"));
			} catch(ObjectNotFoundException e) {
				throw new IOException(e.getMessage(), e);
			}
			try {
				permissionService.assertAnyPermission(AuthorizedKeyServiceImpl.AUTHORIZED_KEY_ASSIGN);
			} catch(AccessDeniedException e) {
				throw new IOException("You do not have the permission to delete a key for " + forUser.getUsername());
			}
		}
		String name = args[args.length-1];
		
		AuthorizedKey key = authorizedKeyService.getAuthorizedKey(forUser, name);
		authorizedKeyService.deleteKey(key);

	}
}
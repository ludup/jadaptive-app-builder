package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.AuthorizedKey;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class AuthorizedKeys extends AbstractTenantAwareCommand {

	@Autowired
	UserService userService; 
	
	@Autowired
	PersonalObjectDatabase<AuthorizedKey> authorizedKeyService; 
	
	public AuthorizedKeys() {
		super("authorized-keys", 
				"User",
				UsageHelper.build("authorized-keys"),
						"List your authorized keys");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
	
		for(AuthorizedKey key : authorizedKeyService.getPersonalObjects(AuthorizedKey.class, user)) {
			console.println(key.getPublicKey());
		}

	}

}

package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.user.User;
import com.jadaptive.plugins.keys.AuthorizedKey;
import com.jadaptive.plugins.keys.AuthorizedKeyService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class AuthorizedKeys extends AbstractTenantAwareCommand {
	
	@Autowired
	private AuthorizedKeyService authorizedKeyService;
	
	public AuthorizedKeys() {
		super("authorized-keys", 
				"Key Management",
				UsageHelper.build("authorized-keys [--user <username>]"),
						"List your own, or another users authorized keys");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
	
		User user = getCurrentUser();
		if(CliHelper.hasOption(args, 'u', "user")) {
			user = userService.getUser(CliHelper.getValue(args, 'u', "user"));
		}
		
		Collection<AuthorizedKey> keys;
		if(CliHelper.hasOption(args, 't', "tag")) {
			keys =  authorizedKeyService.getAuthorizedKeys(user, 
					CliHelper.getValue(args, 't', "tag"));
		} else {
			keys  = authorizedKeyService.getAuthorizedKeys(user);
		}
		for(AuthorizedKey key :keys) {
			console.println(key.getPublicKey());
		}
	}
}

package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.user.User;
import com.jadaptive.plugins.sshd.AuthorizedKey;
import com.jadaptive.plugins.sshd.AuthorizedKeyService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class AuthorizedKeys extends AbstractTenantAwareCommand {
	
	@Autowired
	private AuthorizedKeyService authorizedKeyService;
	
	public AuthorizedKeys() {
		super("authorized-keys <user>", 
				"User",
				UsageHelper.build("authorized-keys"),
						"List your own, or another users authorized keys");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
	
		User user = currentUser;
		if(args.length > 1) {
			for(int i=1;i<args.length;i++) {
				if(args[i].startsWith("-")) {
					continue;
				}
				user = userService.findUsername(args[1]);
				break;
			}
		}
		
		Collection<AuthorizedKey> keys;
		if(CliHelper.hasOption(args, 's', "system")) {
			keys =  authorizedKeyService.getSystemKeys(user);
		} else {
			keys  = authorizedKeyService.getAuthorizedKeys(user);
		}
		for(AuthorizedKey key :keys) {
			console.println(key.getPublicKey());
		}
	}

}

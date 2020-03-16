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

public class AuthorizedKeys extends UserCommand {
	
	@Autowired
	private AuthorizedKeyService authorizedKeyService;
	
	public AuthorizedKeys() {
		super("authorized-keys <user>", 
				"Key Management",
				UsageHelper.build("authorized-keys"),
						"List your own, or another users authorized keys");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
	
		User user = getCommandLineUser(false);
		
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

	@Override
	protected void assertPermission() {
		// Any user can list another users keys
	}
}

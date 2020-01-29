package com.jadaptive.app.sshd.commands;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.sshd.AuthorizedKey;
import com.jadaptive.api.sshd.commands.AbstractTenantAwareCommand;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.publickey.SshKeyPairGenerator;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.publickey.SshPrivateKeyFile;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.server.vsession.CliHelper;
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

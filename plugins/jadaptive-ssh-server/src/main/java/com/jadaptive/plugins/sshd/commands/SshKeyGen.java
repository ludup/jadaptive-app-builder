package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.AuthorizedKey;
import com.jadaptive.plugins.sshd.AuthorizedKeyServiceImpl;
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

public class SshKeyGen extends AbstractTenantAwareCommand {

	@Autowired
	UserService userService; 
	
	@Autowired
	PersonalObjectDatabase<AuthorizedKey> authorizedKeyService; 
	
	@Autowired
	PermissionService permissionService; 
	
	public SshKeyGen() {
		super("ssh-keygen", 
				"User",
				UsageHelper.build("ssh-keygen -t [rsa|ecdsa|ed25519] -b [bits] -a [user]",
						"-t, --type              The type of key to generate",
						"-b, --bits              The number of bits required for the key (ignored for ed25519)",
						"-a, --assign            Assign the key to another user (requires administrative or authorizedKey.assign permission"),
						"Generate SSH keys");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
	
		String type;
		try {
			type = CliHelper.getValue(args, 't', "type");
		} catch (UsageException e) {
			type = "ed25519";
		}
		type = type.toLowerCase();
		String bits;
		try {
			bits = CliHelper.getValue(args, 'b', "bits");
		} catch (UsageException e) {
			switch(type) {
			case "ecdsa":
				bits = "256";
				break;
			case "rsa":
				bits = "3076";
				break;
			default:
				bits = "0";
			}
		}
		
		boolean assign = CliHelper.hasOption(args, 'a', "assign");
		User forUser = user;
		if(assign) {
			try {
				forUser = userService.findUsername(CliHelper.getValue(args, 'a', "assign"));
			} catch(EntityNotFoundException e) {
				throw new IOException(e.getMessage(), e);
			}
			try {
				permissionService.assertAnyPermission(AuthorizedKeyServiceImpl.AUTHORIZED_KEY_ASSIGN);
			} catch(AccessDeniedException e) {
				throw new IOException("You do not have the permission to assign a key to " + forUser.getUsername());
			}
		}
		
		if(!StringUtils.isNumeric(bits)) {
			throw new UsageException("bits argument must be a valid number");
		}
		
		String comment = console.readLine("Name: ");
		
		for(int i=0;i<3;i++) {
			char[] newPassword = promptForPassword("Passphrase: ");
			char[] confirmedPassword = promptForPassword("Confirm Passphrase: ");
				
			if(!Arrays.equals(newPassword, confirmedPassword)) {
				console.println("Passphrases do not match!");
				continue;
			}
			
			try {
				
				SshKeyPair pair = SshKeyPairGenerator.generateKeyPair(type, Integer.parseInt(bits));
				
				String publicKey = SshKeyUtils.getOpenSSHFormattedKey(pair.getPublicKey(), comment);
				
				AuthorizedKey key = new AuthorizedKey();
				key.setPublicKey(publicKey);
				key.setName(comment);
				
				authorizedKeyService.saveOrUpdate(key, forUser);
				
				SshPrivateKeyFile file = SshPrivateKeyFileFactory.create(pair, new String(newPassword));
				console.println();
				console.println("*** IMPORTANT ***");
				console.println("Your private has been created and has been printed below.");
				console.println("There is no other record of the private key on this server.");
				console.println("Therefore please copy this to a safe location or it will be lost.");
				console.println();
				console.println(new String(file.getFormattedKey(), "UTF-8"));
				console.println();
				break;
			} catch (NumberFormatException | RepositoryException | EntityException | SshException e) {
				throw new IOException(e.getMessage(), e);
			}
		}

	}

}

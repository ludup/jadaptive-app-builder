package com.jadaptive.plugins.sshd.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.keys.AuthorizedKey;
import com.jadaptive.plugins.keys.AuthorizedKeyService;
import com.jadaptive.plugins.keys.AuthorizedKeyServiceImpl;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.publickey.SshKeyPairGenerator;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.publickey.SshPrivateKeyFile;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.publickey.SshPublicKeyFile;
import com.sshtools.common.publickey.SshPublicKeyFileFactory;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.SshKeyFingerprint;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class SshKeyGen extends AbstractTenantAwareCommand {

	@Autowired
	private UserService userService; 
	
	@Autowired
	private AuthorizedKeyService authorizedKeyService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	public SshKeyGen() {
		super("ssh-keygen", 
				"Key Management",
				UsageHelper.build("ssh-keygen [options]",
						"-t, --type   <rsa|ecdsa|ed25519> The type of key to generate",
						"-b, --bits   <bits>              The number of bits required for the key (ignored for ed25519)",
						"-f, --file   <path>              The file path to save the key to",
						"-a, --assign <user>              Assign the key to a user (requires authorizedKey.assign permission"),
						"Generate an authorized key");
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
				
				SshPublicKeyFile pubFile = SshPublicKeyFileFactory.create(
						pair.getPublicKey(), comment, 
							SshPublicKeyFileFactory.OPENSSH_FORMAT);
				SshPrivateKeyFile file = SshPrivateKeyFileFactory.create(
						pair, new String(newPassword));
				
				AuthorizedKey key = new AuthorizedKey();
				key.setPublicKey(new String(pubFile.getFormattedKey(), "UTF-8"));
				key.setFingerprint(SshKeyUtils.getFingerprint(pair.getPublicKey()));
				key.setType(pair.getPublicKey().getAlgorithm());
				key.setName(comment);
				key.getTags().add(AuthorizedKeyServiceImpl.SSH_TAG);
				
				authorizedKeyService.saveOrUpdate(key, forUser);
				
				if(CliHelper.hasOption(args, 'f', "file")) {
					String path = CliHelper.getValue(args, 'f', "file");
					AbstractFile f = console.getCurrentDirectory().resolveFile(path);
					IOUtils.copy(new ByteArrayInputStream(file.getFormattedKey()), f.getOutputStream());
					console.println();
					console.println(String.format("Private key saved to %s", path));
				} else {
					console.println();
					console.println("*** IMPORTANT ***");
					console.println("Your private has been created and has been printed below.");
					console.println("There is no other record of the private key on this server.");
					console.println("Therefore please copy this to a safe location or it will be lost.");
					console.println();
					console.println(new String(file.getFormattedKey(), "UTF-8"));
				}
				
				console.println();
				console.println(pair.getPublicKey().getFingerprint());
				console.println();
				console.println(SshKeyFingerprint.getBubbleBabble(pair.getPublicKey()));
				console.println();
				
				break;
			} catch (NumberFormatException | RepositoryException | EntityException | SshException e) {
				throw new IOException(e.getMessage(), e);
			}
		}

	}

}

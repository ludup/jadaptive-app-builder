package com.jadaptive.plugins.sshd.commands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.keys.AuthorizedKeyService;
import com.jadaptive.plugins.keys.AuthorizedKeyServiceImpl;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.publickey.SshPrivateKeyFile;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.SshPublicKey;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class ImportKey extends AbstractTenantAwareCommand {

	static Logger log = LoggerFactory.getLogger(ImportKey.class);
	
	@Autowired
	UserService userService; 
	
	@Autowired
	AuthorizedKeyService authorizedKeyService; 
	
	@Autowired
	PermissionService permissionService; 
	
	public ImportKey() {
		super("import-key", 
				"Key Management",
				UsageHelper.build("import-key <filename> [options]",
						"-n, --name   <name>      The name to assign to this key",
						"-a, --assign <user>      Assign the key to another user (requires administrative or authorizedKey.assign permission)"),
						"Import an authorized key");
	}

	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		if(line.wordIndex() == 1) {
			fillCurrentDirectoryCandidates(candidates);
		} 
	}
	
	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {

		if(args.length < 2) {
			throw new UsageException("Missing key filename");
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
				throw new IOException("You do not have the permission to assign a key to " + forUser.getUsername());
			}
		}
		
		String filename = args[1];
		
		AbstractFile f = console.getCurrentDirectory().resolveFile(filename);
		if(!f.exists()) {
			throw new IOException(String.format("%s does not exist", filename));
		}
		
		String comment;
		if(CliHelper.hasOption(args, 'n', "name")) {
			comment = CliHelper.getValue(args, 'n', "name");
		} else {
			comment = console.readLine("Name: ");
		}
		
		SshPublicKey pub;
		
		try {
			pub = SshKeyUtils.getPublicKey(f.getInputStream());
		} catch(IOException ex) {
			pub = fromPrivateKey(filename, console.getConnection());
		}

		try {
			authorizedKeyService.importAuthorizedKey(pub, comment, forUser);
		} catch (SshException e) {
			throw new IOException(e.getMessage(), e);
		}

	}
	
	private SshPublicKey fromPrivateKey(String filename, SshConnection con) throws FileNotFoundException, IOException, PermissionDeniedException {
		
		AbstractFile f = console.getCurrentDirectory().resolveFile(filename);
		if(!f.exists()) {
			throw new IOException(String.format("%s does not exist", filename));
		}
		SshPrivateKeyFile file = null;
		try(InputStream in = f.getInputStream()) {
			file = SshPrivateKeyFileFactory.parse(in);		
		};
	
		SshKeyPair pair = null;
		
		if(file.isPassphraseProtected()) {
			for(int i=0;i<3;i++) {
				try {
					pair = file.toKeyPair(console.getLineReader().readLine("Passphrase: ", '*'));
					break;
				} catch(Exception e) { 
					console.println("Bad passphrase");
				}
			}
		} else {
			try {
				pair = file.toKeyPair(null);
			} catch (InvalidPassphraseException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
		return pair.getPublicKey();
	}
}
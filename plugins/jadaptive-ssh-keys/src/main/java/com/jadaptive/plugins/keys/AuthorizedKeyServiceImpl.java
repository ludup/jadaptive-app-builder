package com.jadaptive.plugins.keys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.publickey.SshKeyPairGenerator;
import com.sshtools.common.publickey.SshKeyUtils;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;


@Service
@Permissions(keys = { AuthorizedKeyService.AUTHORIZED_KEY_ASSIGN })
public class AuthorizedKeyServiceImpl extends AuthenticatedService implements AuthorizedKeyService {
	
	@Autowired
	private PersonalObjectDatabase<AuthorizedKey> objectDatabase; 
	
	@Autowired
	private UserService userService; 
	
	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys(User user) {
		return objectDatabase.getPersonalObjects(AuthorizedKey.class, user);
	}
	
	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys() {
		return getAuthorizedKeys(getCurrentUser());
	}

	@Override
	public void saveOrUpdate(AuthorizedKey key, User user) {
		
		assertAssign(user);
		
		checkDuplicateName(key.getName(), user);
		
		objectDatabase.saveOrUpdate(key, user);
		
	}

	private void assertAssign(User user) {
		if(!getCurrentUser().getUuid().equals(user.getUuid())) {
			assertPermission(AUTHORIZED_KEY_ASSIGN);
		}
	}

	private void checkDuplicateName(String name, User user) {

	}
	
	@Override
	public File createKeyFile(String name, SshKeyPair pair, String passphrase) throws IOException {
		
		File parent = Files.createTempDirectory("jadaptive").toFile();
		parent.mkdirs();
		File zip =	new File(parent, String.format("%s.zip", name));
		
		try(FileOutputStream fos = new FileOutputStream(zip)) {
			try(ZipOutputStream zipOut = new ZipOutputStream(fos)) {
				ZipEntry zipEntry = new ZipEntry(name);
				zipOut.putNextEntry(zipEntry);
				IOUtils.copy(new StringReader(SshKeyUtils.getFormattedKey(pair, passphrase)), zipOut, "UTF-8");
				zipEntry = new ZipEntry(name + ".pub");
				zipOut.putNextEntry(zipEntry);
				IOUtils.copy(new StringReader(SshKeyUtils.getFormattedKey(pair, passphrase)), zipOut, "UTF-8");
			}
		}
	    
		return zip;
	}

	@Override
	public SshKeyPair createAuthorizedKey(PublicKeyType type, String comment, User user) throws IOException, SshException {
		
		SshKeyPair pair = SshKeyPairGenerator.generateKeyPair(type.getAlgorithm(), type.getBits());
		
		AuthorizedKey key = new AuthorizedKey();
		key.setPublicKey(SshKeyUtils.getFormattedKey(pair.getPublicKey(), comment));
		key.setFingerprint(SshKeyUtils.getFingerprint(pair.getPublicKey()));
		key.setType(pair.getPublicKey().getAlgorithm());
		key.setName(comment);
		key.setDeviceKey(false);
		saveOrUpdate(key, user);
		return pair;
	}
	
	@Override
	public AuthorizedKey getAuthorizedKey(User user, String name) {
		return objectDatabase.getPersonalObject(AuthorizedKey.class, user, SearchField.eq("name", name));
	}
	
	@Override
	public AuthorizedKey getAuthorizedKeyByUUID(User user, String uuid) {
		if(NumberUtils.isNumber(uuid)) {
			return objectDatabase.getPersonalObject(AuthorizedKey.class, user, 
					SearchField.eq("id", Long.parseLong(uuid)));
		} else {
			return objectDatabase.getPersonalObject(AuthorizedKey.class, user, 
					SearchField.eq("uuid", uuid));
		}
	}

	@Override
	public void deleteKey(AuthorizedKey key) {
		
		assertAssign(userService.getUserByUUID(key.getOwnerUUID()));
		objectDatabase.deletePersonalObject(key);
	}

	@Override
	public AuthorizedKey importPublicKey(String name, String key, 
			String type, String fingerprint, 
			User user, boolean deviceKey) {
		
		AuthorizedKey authorizedKey = new AuthorizedKey();
		authorizedKey.setPublicKey(key);
		authorizedKey.setType(type);
		authorizedKey.setFingerprint(fingerprint);
		authorizedKey.setName(name);
		authorizedKey.setDeviceKey(deviceKey);
		
		saveOrUpdate(authorizedKey, user);
		return authorizedKey;
	}

	@Override
	public void backupKey(AuthorizedKey key, String encryptedKey) {
		
		
	}

	@Override
	public AuthorizedKey getObjectByUUID(String uuid) {
		return objectDatabase.getObjectByUUID(AuthorizedKey.class, uuid);
	}

	@Override
	public String saveOrUpdate(AuthorizedKey key) {
		objectDatabase.saveOrUpdate(key);
		return key.getUuid();
	}

	@Override
	public void deleteObject(AuthorizedKey key) {
		deleteKey(key);
	}

}

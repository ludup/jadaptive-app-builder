package com.jadaptive.plugins.keys;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.user.User;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;

public interface AuthorizedKeyService extends UUIDObjectService<AuthorizedKey> {

	public static final String AUTHORIZED_KEY_ASSIGN = "authorizedKey.assign";
	public static final String RESOURCE_BUNDLE = "authorizedKeys";
	
	
	Collection<AuthorizedKey> getAuthorizedKeys(User user);

	Collection<AuthorizedKey> getAuthorizedKeys();

	void saveOrUpdate(AuthorizedKey key, User user);

	AuthorizedKey getAuthorizedKey(User user, String name);

	void deleteKey(AuthorizedKey key);

	void backupKey(AuthorizedKey newResource, String encryptedKey);

	AuthorizedKey getAuthorizedKeyByUUID(User user, String uuid);

	AuthorizedKey importPublicKey(String name, String key, String type, 
			String fingerprint, User user, boolean deviceKey);

	SshKeyPair createAuthorizedKey(PublicKeyType type, String comment, User user) throws IOException, SshException;

	File createKeyFile(String name, SshKeyPair pair, String passphrase) throws IOException;
}

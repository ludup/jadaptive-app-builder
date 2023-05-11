package com.jadaptive.plugins.keys;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.user.User;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.SshPublicKey;

public interface AuthorizedKeyService extends UUIDObjectService<AuthorizedKey> {

	public static final String AUTHORIZED_KEY_ASSIGN = "authorizedKey.assign";
	public static final String RESOURCE_BUNDLE = "authorizedKeys";
	
	public static final String AUTHORIZED_KEYS_USER = "authorizedKeysUser";
	public static final String AUTHORIZED_KEYS_ACCOUNT = "authorizedKeysAccount";
	
	Collection<AuthorizedKey> getAuthorizedKeys(User user);

	Collection<AuthorizedKey> getAuthorizedKeys();

	void saveOrUpdate(AuthorizedKey key, User user);

	AuthorizedKey getAuthorizedKey(User user, String name);

	void deleteKey(AuthorizedKey key);

	AuthorizedKey getAuthorizedKeyByUUID(User user, String uuid);

	SshKeyPair createAuthorizedKey(PublicKeyType type, String comment, User user) throws IOException, SshException;

	File createKeyFile(String name, SshKeyPair pair, String passphrase) throws IOException;

	Iterable<AuthorizedKey> getExpiringKeys(Date date);

	AuthorizedKey importPublicKey(String name, String key, String type, String fingerprint, User user, int bits);

	AuthorizedKey importAuthorizedKey(SshPublicKey key, String comment, User user) throws IOException, SshException;
}

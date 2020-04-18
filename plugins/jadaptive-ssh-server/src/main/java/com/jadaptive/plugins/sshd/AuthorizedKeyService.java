package com.jadaptive.plugins.sshd;

import java.util.Collection;

import com.jadaptive.api.user.User;

public interface AuthorizedKeyService {

	public static final String AUTHORIZED_KEY_ASSIGN = "authorizedKey.assign";
	
	public static final String SSH_TAG = "SSH";
	public static final String SYSTEM_TAG = "SYSTEM";
	public static final String DEVICE_TAG = "DEVICE";
	
	Collection<AuthorizedKey> getAuthorizedKeys(User user);

	Collection<AuthorizedKey> getAuthorizedKeys();
	
	Collection<AuthorizedKey> getAuthorizedKeys(User user, String... tags);
	
	Collection<AuthorizedKey> getAuthorizedKeys(String... tags);

	void saveOrUpdate(AuthorizedKey key, String tag, User user);

	Collection<AuthorizedKey> getSystemKeys(User user);

	AuthorizedKey getAuthorizedKey(User user, String name);

	void deleteKey(AuthorizedKey key);

	AuthorizedKey importPublicKey(String name, 
			String key,
			User currentUser, 
			boolean deviceKey,
			String tag);

	void backupKey(AuthorizedKey newResource, String encryptedKey);

	AuthorizedKey getAuthorizedKeyByUUID(User user, String uuid);

	String getSystemKey(KeyType type, String format);

	AuthorizedKey getSystemKey(User user, KeyType keytype);

	AuthorizedKey getSystemKey(KeyType keytype);
}

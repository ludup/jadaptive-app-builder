package com.jadaptive.plugins.keys;

import java.util.Collection;

import com.jadaptive.api.user.User;

public interface AuthorizedKeyService {

	public static final String AUTHORIZED_KEY_ASSIGN = "authorizedKey.assign";
	
	public static final String SSH_TAG = "SSH";
	public static final String SYSTEM_TAG = "SYSTEM";
	public static final String DEVICE_TAG = "DEVICE";
	public static final String WEBAUTHN_TAG = "WEBAUTHN";
	
	Collection<AuthorizedKey> getAuthorizedKeys(User user);

	Collection<AuthorizedKey> getAuthorizedKeys();
	
	Collection<AuthorizedKey> getAuthorizedKeys(User user, String... tags);
	
	Collection<AuthorizedKey> getAuthorizedKeys(String... tags);

	void saveOrUpdate(AuthorizedKey key, User user);

	AuthorizedKey getAuthorizedKey(User user, String name);

	void deleteKey(AuthorizedKey key);

	void backupKey(AuthorizedKey newResource, String encryptedKey);

	AuthorizedKey getAuthorizedKeyByUUID(User user, String uuid);

	AuthorizedKey importPublicKey(String name, String key, String type, 
			String fingerprint, User user,
			String... tag);
}

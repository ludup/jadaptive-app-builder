package com.jadaptive.plugins.sshd;

import java.util.Collection;

import com.jadaptive.api.user.User;

public interface AuthorizedKeyService {

	public static final String SSH_TAG = "SSH";
	public static final String SYSTEM_TAG = "System";
	
	Collection<AuthorizedKey> getAuthorizedKeys(User user);

	Collection<AuthorizedKey> getAuthorizedKeys();

	void saveOrUpdate(AuthorizedKey key, String tag, User user);

	Collection<AuthorizedKey> getSystemKeys(User user);
}

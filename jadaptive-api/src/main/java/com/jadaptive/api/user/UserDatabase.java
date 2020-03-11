package com.jadaptive.api.user;

import org.pf4j.ExtensionPoint;

public interface UserDatabase extends ExtensionPoint {

	void setPassword(User user, char[] password, boolean passwordChangeRequired);

	boolean verifyPassword(User user, char[] password);

	User getUser(String username);

	User getUserByUUID(String uuid);
	
	Iterable<User> iterateUsers();
}

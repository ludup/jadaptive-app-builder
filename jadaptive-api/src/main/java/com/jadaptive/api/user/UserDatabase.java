package com.jadaptive.api.user;

import java.util.Set;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.template.EntityTemplate;

public interface UserDatabase extends ExtensionPoint {

	void setPassword(User user, char[] password, boolean passwordChangeRequired);

	boolean verifyPassword(User user, char[] password);

	User getUser(String username);

	User getUserByUUID(String uuid);
	
	Iterable<User> iterateUsers();

	EntityTemplate getUserTemplate();

	boolean isDatabaseUser(User user);

	Class<? extends User> getUserClass();
	
	Set<UserDatabaseCapabilities> getCapabilities();

	void deleteUser(User user);

	void updateUser(User user);

	void createUser(User user, char[] password, boolean forceChange);
}

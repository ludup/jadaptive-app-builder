package com.jadaptive.api.user;

import java.util.Set;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.template.ObjectTemplate;

public interface UserDatabase extends ExtensionPoint {

	void setPassword(User user, char[] password, boolean passwordChangeRequired);

	boolean verifyPassword(User user, char[] password);

	ObjectTemplate getUserTemplate();

	boolean isDatabaseUser(User user);

	Class<? extends User> getUserClass();
	
	Set<UserDatabaseCapabilities> getCapabilities();

	void deleteUser(User user);

	void updateUser(User user);

	void createUser(User user, char[] password, boolean forceChange);
	
	Integer weight();
}

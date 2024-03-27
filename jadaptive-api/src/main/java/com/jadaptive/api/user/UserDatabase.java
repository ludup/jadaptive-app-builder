package com.jadaptive.api.user;

import java.util.Collection;
import java.util.Set;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.template.ObjectTemplate;

public interface UserDatabase extends ExtensionPoint {

	void setPassword(User user, char[] password, boolean passwordChangeRequired);

	boolean verifyPassword(User user, char[] password);

	boolean hasPassword(User u);
	
	ObjectTemplate getUserTemplate();

	default boolean isDatabaseUser(User user) {
		return user.getClass().equals(getUserClass());
	}

	Class<? extends User> getUserClass();
	
	Set<UserDatabaseCapabilities> getCapabilities();

	default void deleteUser(User user) {
		throw new UnsupportedOperationException();
	}

	default void updateUser(User user) {
		throw new UnsupportedOperationException();
	}

	default void createUser(User user, char[] password, boolean forceChange) {
		throw new UnsupportedOperationException();
	}
	
	Integer weight();

	default User importUser(String username) {
		throw new UnsupportedOperationException();
	};
	
	void registerLogin(User user);

	User findUser(String username);
}

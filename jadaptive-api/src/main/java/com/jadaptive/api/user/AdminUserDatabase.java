package com.jadaptive.api.user;

public interface AdminUserDatabase extends PasswordEnabledUserDatabase {

	User createAdmin(String username, char[] password, String email, boolean forceChange);

	User getPrimaryAdministrator();

	Iterable<User> allObjects();

}

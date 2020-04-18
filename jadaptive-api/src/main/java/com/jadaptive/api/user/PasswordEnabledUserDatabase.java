package com.jadaptive.api.user;

public interface PasswordEnabledUserDatabase<T extends PasswordEnabledUser> extends UserDatabase {

	void setPassword(User user, char[] password, boolean passwordChangeRequired);

	boolean verifyPassword(User user, char[] password);

}

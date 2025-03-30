package com.jadaptive.api.user;

public interface PasswordEnabledUserDatabase extends UserDatabase {

	String PASSWORD_HASH = "passwordHash";

	void setPassword(User user, char[] password, boolean passwordChangeRequired);

	boolean verifyPassword(User user, char[] password);

}

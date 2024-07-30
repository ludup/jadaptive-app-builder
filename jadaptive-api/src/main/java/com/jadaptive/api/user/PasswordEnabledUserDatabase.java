package com.jadaptive.api.user;

public interface PasswordEnabledUserDatabase extends UserDatabase {

	void setPassword(User user, char[] password, boolean passwordChangeRequired);

	boolean verifyPassword(User user, char[] password);


}

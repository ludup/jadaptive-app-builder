package com.jadaptive.user;

public interface UserService {

	User createUser(String username, char[] password, String name);

	boolean verifyPassword(User username, char[] password);

	User findUsername(String string);

	void setPassword(User user, char[] newPassword);

}

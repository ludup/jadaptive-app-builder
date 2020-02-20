package com.jadaptive.api.user;

public interface UserService {

	User createUser(String username, char[] password, String name, boolean passwordChangeRequired);

	boolean verifyPassword(User username, char[] password);

	User findUsername(String username);

	void setPassword(User user, char[] newPassword);

	User getUser(String uuid);

	void changePassword(User user, char[] oldPassword, char[] newPassword);

	void changePassword(User user, char[] newPassword);

	Iterable<? extends User> iterateUsers();

}

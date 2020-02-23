package com.jadaptive.api.user;

public interface UserService {

	public static final String CHANGE_PASSWORD_PERMISSION = "user.changePassword";
	public static final String SET_PASSWORD_PERMISSION = "user.setPassword";
	public static final String USER_RESOURCE_KEY = "user";
	
	User createUser(String username, char[] password, String name, boolean passwordChangeRequired);

	boolean verifyPassword(User username, char[] password);

	User findUsername(String username);

	void setPassword(User user, char[] newPassword, boolean passwordChangeRequired);

	User getUser(String uuid);

	void changePassword(User user, char[] oldPassword, char[] newPassword);

	void changePassword(User user, char[] newPassword, boolean passwordChangeRequired);

	Iterable<? extends User> iterateUsers();

}

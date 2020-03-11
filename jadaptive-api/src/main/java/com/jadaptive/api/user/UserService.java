package com.jadaptive.api.user;

public interface UserService {

	public static final String CHANGE_PASSWORD_PERMISSION = "user.changePassword";
	public static final String SET_PASSWORD_PERMISSION = "user.setPassword";
	
	public static final String USER_RESOURCE_KEY = "user";
	
	boolean verifyPassword(User username, char[] password);

	User getUser(String username);

	void setPassword(User user, char[] newPassword, boolean passwordChangeRequired);

	User getUserByUUID(String uuid);

	void changePassword(User user, char[] oldPassword, char[] newPassword);

	void changePassword(User user, char[] newPassword, boolean passwordChangeRequired);

	Iterable<? extends User> iterateUsers();

}

package com.jadaptive.api.user;

import java.util.Collection;
import java.util.Map;

import com.jadaptive.api.repository.UUIDDocumentService;
import com.jadaptive.api.template.ObjectTemplate;

public interface UserService extends UUIDDocumentService {

	public static final String CHANGE_PASSWORD_PERMISSION = "users.changePassword";
	public static final String SET_PASSWORD_PERMISSION = "users.setPassword";
	
	public static final String USER_RESOURCE_KEY = "users";
	
	public static final String READ_PERMISSION = "users.read";
	public static final String READ_WRITE_PERMISSION = "users.readWrite";
	
	boolean verifyPassword(User username, char[] password);

	User getUser(String username);

	void setPassword(User user, char[] newPassword, boolean passwordChangeRequired);

	User getUserByUUID(String uuid);

	void changePassword(User user, char[] oldPassword, char[] newPassword);

	void changePassword(User user, char[] newPassword, boolean passwordChangeRequired);

	Iterable<? extends User> allUsers();

	User getUserByEmail(String email);

	Collection<ObjectTemplate> getCreateUserTemplates();

	void deleteUser(User confirmedUser);

	void updateUser(User user);

	void createUser(User user, char[] charArray, boolean forceChange);

	boolean supportsLogin(User user);

	Map<String, String> getUserProperties(User user);


}

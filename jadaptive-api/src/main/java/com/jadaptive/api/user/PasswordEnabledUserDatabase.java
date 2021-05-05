package com.jadaptive.api.user;

import com.jadaptive.api.repository.UUIDObjectService;

public interface PasswordEnabledUserDatabase extends UserDatabase, UUIDObjectService<User> {

	void setPassword(User user, char[] password, boolean passwordChangeRequired);

	boolean verifyPassword(User user, char[] password);

}

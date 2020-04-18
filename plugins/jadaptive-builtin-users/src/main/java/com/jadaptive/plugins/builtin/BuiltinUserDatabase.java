package com.jadaptive.plugins.builtin;

import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserDatabase;

public interface BuiltinUserDatabase extends UserDatabase {

	User createUser(String username, String name, String email, char[] password, boolean passwordChangeRequired);

	void saveOrUpdate(User user);

	void deleteUser(User user);

	User getUserByEmail(String email);
}

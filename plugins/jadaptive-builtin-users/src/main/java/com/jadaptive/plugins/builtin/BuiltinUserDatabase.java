package com.jadaptive.plugins.builtin;

import com.jadaptive.api.user.PasswordEnabledUserDatabase;
import com.jadaptive.api.user.User;

public interface BuiltinUserDatabase extends PasswordEnabledUserDatabase {

	BuiltinUser createUser(String username, String name, String email, char[] password, boolean passwordChangeRequired);

	void deleteUser(BuiltinUser user);

	Iterable<User> allObjects();

}

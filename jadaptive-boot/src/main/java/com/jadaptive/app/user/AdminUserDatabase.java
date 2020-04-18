package com.jadaptive.app.user;

import com.jadaptive.api.user.PasswordEnabledUserDatabase;
import com.jadaptive.api.user.User;

public interface AdminUserDatabase extends PasswordEnabledUserDatabase<AdminUser> {

	User createAdmin(char[] password, boolean forceChange);

}

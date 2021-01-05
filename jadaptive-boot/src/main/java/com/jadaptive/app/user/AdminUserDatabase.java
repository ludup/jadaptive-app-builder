package com.jadaptive.app.user;

import com.jadaptive.api.user.PasswordEnabledUserDatabase;

public interface AdminUserDatabase extends PasswordEnabledUserDatabase {

	AdminUser createAdmin(char[] password, boolean forceChange);

}

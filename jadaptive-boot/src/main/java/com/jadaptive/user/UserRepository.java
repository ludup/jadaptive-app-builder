package com.jadaptive.user;

import com.jadaptive.db.AbstractObjectDatabase;

public interface UserRepository extends AbstractObjectDatabase {

	void createUser(DefaultUser user, char[] password);

}

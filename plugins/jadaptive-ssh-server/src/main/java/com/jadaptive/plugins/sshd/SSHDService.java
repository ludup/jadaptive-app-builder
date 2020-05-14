package com.jadaptive.plugins.sshd;

import com.jadaptive.api.user.User;
import com.sshtools.common.files.AbstractFileFactory;

public interface SSHDService {

	public static final String USER = "userObject";
	public static final String TENANT = "tenantObject";
	
	AbstractFileFactory<?> getFileFactory(User user);

}

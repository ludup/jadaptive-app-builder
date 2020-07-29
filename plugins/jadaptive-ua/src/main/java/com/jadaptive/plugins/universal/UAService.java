package com.jadaptive.plugins.universal;

import java.util.Properties;

import com.jadaptive.api.user.User;

public interface UAService {

	Properties getCredentials(User user);

	boolean hasCredentials(User user);

	void saveRegistration(User user, Properties properties);

}

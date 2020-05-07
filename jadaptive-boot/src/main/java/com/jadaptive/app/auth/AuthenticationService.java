package com.jadaptive.app.auth;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.tenant.Tenant;

public interface AuthenticationService {

	public static final String USER_LOGIN_PERMISSION =  "users.login";
	
	Session logonUser(String username, String password, Tenant tenant, String remoteAddress, String userAgent);

}

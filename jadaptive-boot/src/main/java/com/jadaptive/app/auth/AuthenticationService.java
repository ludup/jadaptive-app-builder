package com.jadaptive.app.auth;

import com.jadaptive.api.session.Session;
import com.jadaptive.api.tenant.Tenant;

public interface AuthenticationService {

	Session logonUser(String username, String password, Tenant tenant, String remoteAddress, String userAgent);

}

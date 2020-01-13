package com.jadaptive.app.auth;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	private UserService userService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public Session logonUser(String username, String password, 
			Tenant tenant, String remoteAddress, String userAgent) {
		
		permissionService.setupSystemContext();
		
		try {
			User user = userService.findUsername(username);
			if(Objects.isNull(user)) {
				throw new AccessDeniedException("Bad username or password");
			}
			
			if(!userService.verifyPassword(user, password.toCharArray())) {
				throw new AccessDeniedException("Bad username or password");
			}
			
			return sessionService.createSession(tenant, 
					user, remoteAddress, userAgent);
		
		} finally {
			permissionService.clearUserContext();
		}
	}
}

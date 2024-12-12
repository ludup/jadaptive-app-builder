package com.jadaptive.app.auth.oauth2;

import java.util.Optional;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.auth.oauth2.OAuth2Request;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;

@Extension
public class AllApiAccessScope implements OAuth2Scope {

	public static final String ALL_API_ACCESS = "allApiAccess";
	
	@Autowired
	private PermissionService permissionService;

	public AllApiAccessScope() {
	}

	@Override
	public String getBundle() {
		return "oauth2";
	}

	@Override
	public String getId() {
		return ALL_API_ACCESS;
	}

	@Override
	public void verifyPermissions(Optional<OAuth2Request> oauthRequest, User principal) throws AccessDeniedException {
		if(!permissionService.isAdministrator(principal)) {
			throw new AccessDeniedException(String.format("%s is not an Administrator", 
					principal.getUsername()));
		}
	}
}

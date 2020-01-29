package com.jadaptive.app.sshd;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.sshd.AuthorizedKey;
import com.jadaptive.api.sshd.AuthorizedKeyService;
import com.jadaptive.api.user.User;

@Service
public class AuthorizedKeyServiceImpl implements AuthorizedKeyService {

	public static final String AUTHORIZED_KEY_ASSIGN = "authorizedKey.assign";
	
	@Autowired
	private PersonalObjectDatabase<AuthorizedKey> objectDatabase; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@PostConstruct
	private void postConstruct() {
		permissionService.registerCustomPermission(AUTHORIZED_KEY_ASSIGN);
	}
	
	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys(User user) {
		return objectDatabase.getPersonalObjects(AuthorizedKey.class, user);
	}

}

package com.jadaptive.plugins.sshd;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.user.User;

@Service
@Permissions(keys = { AuthorizedKeyServiceImpl.AUTHORIZED_KEY_ASSIGN })
public class AuthorizedKeyServiceImpl implements AuthorizedKeyService {

	public static final String AUTHORIZED_KEY_ASSIGN = "authorizedKey.assign";
	
	@Autowired
	private PersonalObjectDatabase<AuthorizedKey> objectDatabase; 
	
	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys(User user) {
		return objectDatabase.getPersonalObjects(AuthorizedKey.class, user);
	}

}

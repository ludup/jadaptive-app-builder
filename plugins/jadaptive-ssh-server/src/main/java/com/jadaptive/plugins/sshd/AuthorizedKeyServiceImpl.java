package com.jadaptive.plugins.sshd;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
@Permissions(keys = { AuthorizedKeyService.AUTHORIZED_KEY_ASSIGN })
public class AuthorizedKeyServiceImpl extends AuthenticatedService implements AuthorizedKeyService {
	
	@Autowired
	private PersonalObjectDatabase<AuthorizedKey> objectDatabase; 
	
	@Autowired
	private UserService userService; 
	
	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys(User user) {
		return objectDatabase.getPersonalObjects(AuthorizedKey.class, user, SearchField.in("tags", SSH_TAG));
	}
	
	@Override
	public Collection<AuthorizedKey> getSystemKeys(User user) {
		return objectDatabase.getPersonalObjects(AuthorizedKey.class, user, SearchField.in("tags", SYSTEM_TAG));
	}
	
	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys() {
		return getAuthorizedKeys(getCurrentUser());
	}

	@Override
	public void saveOrUpdate(AuthorizedKey key, String tag, User user) {
		
		assertAssign(user);
		
		key.getTags().add(tag);
		
		checkDuplicateName(key.getName(), user);
		
		objectDatabase.saveOrUpdate(key, user);
		
	}

	private void assertAssign(User user) {
		if(!getCurrentUser().getUuid().equals(user.getUuid())) {
			assertPermission(AUTHORIZED_KEY_ASSIGN);
		}
	}

	private void checkDuplicateName(String name, User user) {

	}

	@Override
	public AuthorizedKey getAuthorizedKey(User user, String name) {
		return objectDatabase.getPersonalObject(AuthorizedKey.class, user, SearchField.eq("name", name));
	}

	@Override
	public void deleteKey(AuthorizedKey key) {
		
		assertAssign(userService.getUserByUUID(key.getOwnerUUID()));
		objectDatabase.deletePersonalObject(key);
	}

}

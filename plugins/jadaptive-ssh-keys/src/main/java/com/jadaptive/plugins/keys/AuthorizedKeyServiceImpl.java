package com.jadaptive.plugins.keys;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.math.NumberUtils;
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
	
	@PostConstruct
	private void postConstruct() {
		System.out.println();
	}
	
	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys(User user) {
		return objectDatabase.getPersonalObjects(AuthorizedKey.class, user, SearchField.in("tags", SSH_TAG));
	}
	
	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys() {
		return getAuthorizedKeys(getCurrentUser());
	}

	@Override
	public void saveOrUpdate(AuthorizedKey key, User user) {
		
		assertAssign(user);
		
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
	public AuthorizedKey getAuthorizedKeyByUUID(User user, String uuid) {
		if(NumberUtils.isNumber(uuid)) {
			return objectDatabase.getPersonalObject(AuthorizedKey.class, user, 
					SearchField.eq("id", Long.parseLong(uuid)));
		} else {
			return objectDatabase.getPersonalObject(AuthorizedKey.class, user, 
					SearchField.eq("uuid", uuid));
		}
	}

	@Override
	public void deleteKey(AuthorizedKey key) {
		
		assertAssign(userService.getUserByUUID(key.getOwnerUUID()));
		objectDatabase.deletePersonalObject(key);
	}

	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys(User user, String... tags) {
		return objectDatabase.getPersonalObjects(AuthorizedKey.class, user, 
				SearchField.in("tags", Arrays.asList(tags)));
	}

	@Override
	public Collection<AuthorizedKey> getAuthorizedKeys(String... tags) {
		return getAuthorizedKeys(getCurrentUser(), tags);
	}

	@Override
	public AuthorizedKey importPublicKey(String name, String key, 
			String type, String fingerprint, 
			User user, String... tags) {
		
		AuthorizedKey authorizedKey = new AuthorizedKey();
		authorizedKey.setPublicKey(key);
		authorizedKey.setType(type);
		authorizedKey.setFingerprint(fingerprint);
		authorizedKey.setName(name);

		authorizedKey.getTags().addAll(Arrays.asList(tags));
		
		saveOrUpdate(authorizedKey, user);
		return authorizedKey;
	}

	@Override
	public void backupKey(AuthorizedKey key, String encryptedKey) {
		
		
	}

	@Override
	public AuthorizedKey getObjectByUUID(String uuid) {
		return objectDatabase.getObjectByUUID(AuthorizedKey.class, uuid);
	}

	@Override
	public String saveOrUpdate(AuthorizedKey key) {
		objectDatabase.saveOrUpdate(key);
		return key.getUuid();
	}



}

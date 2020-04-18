package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.db.PersonalObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.publickey.SshKeyUtils;

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
	public AuthorizedKey getAuthorizedKeyByUUID(User user, String uuid) {
		return objectDatabase.getPersonalObject(AuthorizedKey.class, user, SearchField.eq("uuid", uuid));
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
	public AuthorizedKey importPublicKey(String name, String key, User user, boolean deviceKey, String tag) {
		
		AuthorizedKey authorizedKey = new AuthorizedKey();
		authorizedKey.setPublicKey(key);
		authorizedKey.setName(name);

		if(deviceKey) {
			authorizedKey.getTags().add("DEVICE");
		}
		
		saveOrUpdate(authorizedKey, tag, user);
		return authorizedKey;
	}

	@Override
	public void backupKey(AuthorizedKey key, String encryptedKey) {
		
		
	}

	@Override
	public String getSystemKey(KeyType type, String format) {

		for(AuthorizedKey key : getSystemKeys(getCurrentUser())) {
			if(key.getType()==type) {
				switch(format.toUpperCase()) {
				case "PEM":
					StringWriter out = new StringWriter();

					try(JcaPEMWriter writer = new JcaPEMWriter(out)) {
						writer.writeObject(SshKeyUtils.getPublicKey(key.getPublicKey()).getJCEPublicKey());
						writer.flush();
						return out.toString();
					} catch (IOException e) {
						throw new IllegalStateException(e.getMessage(), e);
					} 
					
				default:
					return key.getPublicKey();
				}
			}
		}
		
		throw new EntityNotFoundException(type + " not found for user " + getCurrentUser().getName());
	}

	@Override
	public AuthorizedKey getSystemKey(KeyType keytype) {
		return getSystemKey(getCurrentUser(), keytype);
	}
	
	@Override
	public AuthorizedKey getSystemKey(User user, KeyType keytype) {
		
		for(AuthorizedKey key : getSystemKeys(user)) {
			if(key.getType()==keytype) {
				return key;
			}
		}
		
		throw new EntityNotFoundException(keytype + " not found for user " + user.getName());
	}

}

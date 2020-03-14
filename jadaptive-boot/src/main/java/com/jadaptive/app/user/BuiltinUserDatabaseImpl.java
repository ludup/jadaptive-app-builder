package com.jadaptive.app.user;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.user.BuiltinUserDatabase;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.PasswordEncryptionType;
import com.jadaptive.utils.PasswordUtils;

@Extension
public class BuiltinUserDatabaseImpl extends AuthenticatedService implements BuiltinUserDatabase {

	@Autowired
	private TenantAwareObjectDatabase<BuiltinUser> objectDatabase;
	
	@Override
	public BuiltinUser getUser(String username) {
		return objectDatabase.get(BuiltinUser.class, 
				SearchField.or(SearchField.eq("username", username), SearchField.eq("email", username)));
	}
	
	@Override
	public User getUserByUUID(String uuid) {
		return objectDatabase.get(uuid, BuiltinUser.class);
	}
	
	@Override
	public User createUser(String username, String name, String email, char[] password, boolean passwordChangeRequired) {
		
		assertWrite(UserService.USER_RESOURCE_KEY);
		
		BuiltinUser user = new BuiltinUser();
		user.setUsername(username);
		user.setName(name);
		user.setEmail(email);
		
		setPassword(user, password, passwordChangeRequired);
		objectDatabase.saveOrUpdate(user);
		return user;
	}
	
	@Override
	public void setPassword(User u, char[] password, boolean passwordChangeRequired) {
		
		try {
			
			if(!(u instanceof BuiltinUser)) {
				throw new IllegalStateException("Object is not a Builtin user object");
			}
			
			BuiltinUser user = (BuiltinUser)u;
			byte[] salt = PasswordUtils.generateSalt();
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					password, 
					salt, 
					PasswordEncryptionType.PBKDF2_SHA512_100000);
			
			user.setEncodingType(PasswordEncryptionType.PBKDF2_SHA512_100000);
			user.setSalt(Base64.getEncoder().encodeToString(salt));
			user.setEncodedPassword(Base64.getEncoder().encodeToString(encodedPassword));
			user.setPasswordChangeRequired(passwordChangeRequired);
			
			objectDatabase.saveOrUpdate(user);
			
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new EntityException(e);
		}
	}
	
	@Override
	public boolean verifyPassword(User u, char[] password) {
		
		try {
			if(!(u instanceof BuiltinUser)) {
				throw new IllegalStateException("Object is not a Builtin user object");
			}
			BuiltinUser user = (BuiltinUser)u;
			byte[] salt = Base64.getDecoder().decode(user.getSalt());
			byte[] encodedPassword = PasswordUtils.getEncryptedPassword(
					password, 
					salt, 
					PasswordEncryptionType.PBKDF2_SHA512_100000);
			
			byte[] storedPassword = Base64.getDecoder().decode(user.getEncodedPassword());
			
			return Arrays.equals(encodedPassword, storedPassword);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new EntityException(e);
		}
	}

	@Override
	public Iterable<User> iterateUsers() {
		return new UserIterable(objectDatabase.iterator(BuiltinUser.class));
	}
	
	class UserIterable implements Iterable<User> {

		Iterable<BuiltinUser> iterator;
		public UserIterable(Iterable<BuiltinUser> iterator) {
			this.iterator = iterator;
		}

		@Override
		public Iterator<User> iterator() {
			return new ConvertingIterator(iterator.iterator());
		}
	
		class ConvertingIterator implements Iterator<User> {

			Iterator<BuiltinUser> iterator;
			
			public ConvertingIterator(Iterator<BuiltinUser> iterator) {
				this.iterator = iterator;
			}

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public User next() {
				return iterator.next();
			}
		}
	}

	@Override
	public void saveOrUpdate(User user) {
		assertWrite(UserService.USER_RESOURCE_KEY);
		objectDatabase.saveOrUpdate((BuiltinUser) user); 
	}

	@Override
	public void deleteUser(User user) {
		assertWrite(UserService.USER_RESOURCE_KEY);
		objectDatabase.delete((BuiltinUser) user); 
	}
}

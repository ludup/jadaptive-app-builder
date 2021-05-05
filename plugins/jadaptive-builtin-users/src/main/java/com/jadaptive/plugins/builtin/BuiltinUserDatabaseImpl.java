package com.jadaptive.plugins.builtin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.user.PasswordEnabledUserDatabaseImpl;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserDatabaseCapabilities;
import com.jadaptive.api.user.UserImpl;
import com.jadaptive.api.user.UserService;

@Extension
public class BuiltinUserDatabaseImpl extends PasswordEnabledUserDatabaseImpl implements BuiltinUserDatabase {

	@Autowired
	private TenantAwareObjectDatabase<BuiltinUser> objectDatabase;
	
	@Autowired
	private TemplateService templateService; 
	
	private final Set<UserDatabaseCapabilities> capabilities = new HashSet<>(
			Arrays.asList(UserDatabaseCapabilities.MODIFY_PASSWORD,
					UserDatabaseCapabilities.CREATE,
					UserDatabaseCapabilities.UPDATE,
					UserDatabaseCapabilities.DELETE,
					UserDatabaseCapabilities.LOGON));
	
	@Override
	public BuiltinUser getUser(String username) {
		return objectDatabase.get(BuiltinUser.class, 
				SearchField.or(SearchField.eq("username", username), SearchField.eq("email", username)));
	}
	
	public BuiltinUser getUserByEmail(String email) {
		return objectDatabase.get(BuiltinUser.class, 
				SearchField.eq("email", email));
	}
	
	@Override
	public BuiltinUser getUserByUUID(String uuid) {
		return objectDatabase.get(uuid, BuiltinUser.class);
	}
	
	public BuiltinUser createUser(String username, String name, String email, char[] password, boolean passwordChangeRequired) {
		
		assertWrite(UserService.USER_RESOURCE_KEY);
		
		BuiltinUser user = new BuiltinUser();
		user.setUsername(username);
		user.setName(name);
		user.setEmail(email);
		
		createUser(user, password, passwordChangeRequired);
		return user;
	}

	@Override
	public Iterable<User> allObjects() {
		return new UserIterable(objectDatabase.list(BuiltinUser.class));
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
			public BuiltinUser next() {
				return iterator.next();
			}
		}
	}

	@Override
	public void deleteUser(User user) {
		assertWrite(UserService.USER_RESOURCE_KEY);
		objectDatabase.delete((BuiltinUser) user); 
	}

	@Override
	public ObjectTemplate getUserTemplate() {
		return templateService.get(BuiltinUser.RESOURCE_KEY);
	}

	@Override
	public boolean isDatabaseUser(User user) {
		return user instanceof BuiltinUser;
	}

	@Override
	public Class<BuiltinUser> getUserClass() {
		return BuiltinUser.class;
	}

	@Override
	public Set<UserDatabaseCapabilities> getCapabilities() {
		return Collections.unmodifiableSet(capabilities);
	}

	@Override
	public void updateUser(User user) {
		objectDatabase.saveOrUpdate((BuiltinUser) user); 
	}

	@Override
	public void createUser(User user, char[] password, boolean passwordChangeRequired) {
		
		setPassword(user, password, passwordChangeRequired);
		objectDatabase.saveOrUpdate((BuiltinUser) user); 
		
	}
	
	@Override
	public Integer weight() {
		return Integer.MIN_VALUE + 2;
	}

	@Override
	public void deleteUser(BuiltinUser user) {
		
		assertWrite(UserService.USER_RESOURCE_KEY);
		
		if(user.isSystem()) {
			throw new ObjectException(String.format("%s cannot be deleted", user.getUsername()));
		}
		
		objectDatabase.delete(user);
		
	}
	
	@Override
	public void deleteObject(User user) {
		
		assertWrite(UserService.USER_RESOURCE_KEY);
		
		if(user.isSystem()) {
			throw new ObjectException(String.format("%s cannot be deleted", user.getUsername()));
		}
		
		objectDatabase.delete((BuiltinUser)user);
		
	}

}

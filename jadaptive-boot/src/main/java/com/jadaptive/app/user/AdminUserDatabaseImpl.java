package com.jadaptive.app.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.EntityNotFoundException;
import com.jadaptive.api.template.EntityTemplate;
import com.jadaptive.api.template.EntityTemplateService;
import com.jadaptive.api.user.PasswordEnabledUserDatabaseImpl;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserDatabaseCapabilities;

@Extension
public class AdminUserDatabaseImpl extends PasswordEnabledUserDatabaseImpl<AdminUser> implements AdminUserDatabase {

	public static final String ADMIN_USER_UUID = "ac94a50d-c8db-4297-bbf8-dd0adab5c2e6";
	
	@Autowired
	private TenantAwareObjectDatabase<AdminUser> objectDatabase;
	
	@Autowired
	private EntityTemplateService templateService; 
	
	private final Set<UserDatabaseCapabilities> capabilities = new HashSet<>(
			Arrays.asList(UserDatabaseCapabilities.MODIFY_PASSWORD,
					UserDatabaseCapabilities.LOGON));
	
	@Override
	public User getUser(String username) {
		if(!"admin".equals(username)) {
			throw new EntityNotFoundException("Not an Administration user");
		}
		return objectDatabase.get(AdminUser.class, SearchField.eq("uuid", ADMIN_USER_UUID));
	}

	@Override
	public User createAdmin(char[] password, boolean forceChange) {
		
		AdminUser user = new AdminUser();
		user.setUuid(ADMIN_USER_UUID);
		setPassword(user, password, forceChange);
		objectDatabase.saveOrUpdate(user);
		return user;
	}
	
	@Override
	public User getUserByUUID(String uuid) {
		return objectDatabase.get(AdminUser.class, SearchField.eq("uuid", uuid));
	}

	@Override
	public Iterable<User> iterateUsers() {
		return new ArrayList<User>(Arrays.asList(
				objectDatabase.get(AdminUser.class, 
						SearchField.eq("uuid", ADMIN_USER_UUID))));
	}

	@Override
	public EntityTemplate getUserTemplate() {
		return templateService.get(AdminUser.RESOURCE_KEY);
	}

	@Override
	public boolean isDatabaseUser(User user) {
		return user instanceof AdminUser;
	}

	@Override
	public Class<? extends User> getUserClass() {
		return AdminUser.class;
	}

	@Override
	public Set<UserDatabaseCapabilities> getCapabilities() {
		return Collections.unmodifiableSet(capabilities);
	}

	@Override
	public void deleteUser(User user) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void updateUser(User user) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void createUser(User user, char[] password, boolean forceChange) {
		throw new UnsupportedOperationException();
	}

}

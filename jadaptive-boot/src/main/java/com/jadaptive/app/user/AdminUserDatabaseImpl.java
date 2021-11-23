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
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.user.AdminUserDatabase;
import com.jadaptive.api.user.PasswordEnabledUserDatabaseImpl;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserDatabaseCapabilities;

@Extension
public class AdminUserDatabaseImpl extends PasswordEnabledUserDatabaseImpl implements AdminUserDatabase {

	@Autowired
	private TenantAwareObjectDatabase<AdminUser> objectDatabase;
	
	@Autowired
	private TemplateService templateService; 
	
	@Autowired
	private RoleService roleService; 
	
	private final Set<UserDatabaseCapabilities> capabilities = new HashSet<>(
			Arrays.asList(UserDatabaseCapabilities.MODIFY_PASSWORD,
					UserDatabaseCapabilities.LOGON));
	
	@Override
	public User getUser(String username) {
		return objectDatabase.get(AdminUser.class, SearchField.eq("username", username));
	}

	@Override
	public AdminUser createAdmin(String username, char[] password, String email, boolean forceChange) {
		
		AdminUser user = new AdminUser();
		user.setUsername(username);
		user.setEmail(email);
		user.setSystem(true);
		setPassword(user, password, forceChange);
		objectDatabase.saveOrUpdate(user);
		
		roleService.assignRole(roleService.getAdministrationRole(), user);
		return user;
	}
	
	@Override
	public AdminUser getUserByUUID(String uuid) {
		return objectDatabase.get(AdminUser.class, SearchField.eq("uuid", uuid));
	}

	@Override
	public Iterable<User> allObjects() {
		ArrayList<User> users = new ArrayList<>();
		for(AdminUser user : objectDatabase.list(AdminUser.class)) {
			users.add(user);
		}
		return users;
	}

	@Override
	public ObjectTemplate getUserTemplate() {
		return templateService.get(AdminUser.RESOURCE_KEY);
	}

	@Override
	public boolean isDatabaseUser(User user) {
		return user instanceof AdminUser;
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
		assertWrite(AdminUser.RESOURCE_KEY);
		objectDatabase.saveOrUpdate((AdminUser)user);
	}

	@Override
	public void createUser(User user, char[] password, boolean forceChange) {
		throw new UnsupportedOperationException();
	}
	
	public Integer getOrder() { return Integer.MIN_VALUE + 1; }

	@Override
	public Integer weight() {
		return Integer.MIN_VALUE;
	}

	@Override
	public Class<? extends User> getUserClass() {
		return AdminUser.class;
	}

	@Override
	public void deleteObject(User object) {
		objectDatabase.delete((AdminUser)object);
	}

	@Override
	public void deleteObjectByUUID(String uuid) {
		deleteObject(getObjectByUUID(uuid));
	}
}

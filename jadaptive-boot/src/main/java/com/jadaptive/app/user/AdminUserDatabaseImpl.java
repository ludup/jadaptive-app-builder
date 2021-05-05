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
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.TemplateService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.user.PasswordEnabledUserDatabaseImpl;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserDatabaseCapabilities;
import com.jadaptive.api.user.UserImpl;

@Extension
public class AdminUserDatabaseImpl extends PasswordEnabledUserDatabaseImpl implements AdminUserDatabase, TenantAware {

	public static final String ADMIN_USER_UUID = "ac94a50d-c8db-4297-bbf8-dd0adab5c2e6";
	
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
		if(!"admin".equals(username)) {
			throw new ObjectNotFoundException("Not an Administration user");
		}
		return objectDatabase.get(AdminUser.class, SearchField.eq("uuid", ADMIN_USER_UUID));
	}

	@Override
	public AdminUser createAdmin(char[] password, boolean forceChange) {
		
		AdminUser user = new AdminUser();
		user.setUuid(ADMIN_USER_UUID);
		user.setSystem(true);
		setPassword(user, password, forceChange);
		objectDatabase.saveOrUpdate(user);
		return user;
	}
	
	@Override
	public AdminUser getUserByUUID(String uuid) {
		return objectDatabase.get(AdminUser.class, SearchField.eq("uuid", uuid));
	}

	@Override
	public Iterable<User> allObjects() {
		return new ArrayList<User>(Arrays.asList(
				objectDatabase.get(AdminUser.class, 
						SearchField.eq("uuid", ADMIN_USER_UUID))));
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

	@Override
	public void initializeSystem(boolean newSchema) {
		if(newSchema) {
			User user = createAdmin("admin".toCharArray(), true);
			roleService.assignRole(roleService.getAdministrationRole(), user);
		}
	}

	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {

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
		// TODO
		
	}


}

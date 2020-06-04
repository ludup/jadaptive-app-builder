package com.jadaptive.app.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserAware;
import com.jadaptive.api.user.UserDatabase;
import com.jadaptive.api.user.UserDatabaseCapabilities;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.CompoundIterable;

@Service
public class UserServiceImpl extends AuthenticatedService implements UserService, TenantAware {
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	private Map<Class<? extends User>,UserDatabase> userDatabases = new HashMap<>();
	
	private void loadUserDatabases() {
		if(userDatabases.isEmpty()) {
			for(UserDatabase userDatabase : applicationService.getBeans(UserDatabase.class)) {
				userDatabases.put(userDatabase.getUserClass(), userDatabase);
			}
		}
	}
	
	private UserDatabase getDatabase(User user) {
		UserDatabase database = userDatabases.get(user.getClass());
		if(Objects.isNull(database)) {
			throw new IllegalStateException(
					String.format("User type %s does not have a corresponding database",
							user.getClass().getSimpleName()));
		}
		return database;
	}
	
	@Override
	public User getUserByUUID(String uuid) {
		
		User user = null;
		
		for(UserDatabase userDatabase : userDatabases.values()) {
			try {
				user = userDatabase.getUserByUUID(uuid);
				if(Objects.nonNull(user)) {
					break;
				}
			} catch(ObjectNotFoundException e) { }
		}
		
		if(Objects.isNull(user)) {
			throw new ObjectNotFoundException(String.format("User with id %s not found", uuid));
		}
		return user;
	}

	@Override
	public boolean verifyPassword(User user, char[] password) {
		try {
			return getDatabase(user).verifyPassword(user, password);
		} catch(ObjectNotFoundException e) {
			return false;
		}
	}


	@Override
	public User getUser(String username) {

		User user = null;
		
		for(UserDatabase userDatabase : userDatabases.values()) {
			try {
				user = userDatabase.getUser(username);
				if(Objects.nonNull(user)) {
					break;
				}
			} catch(ObjectNotFoundException e) { }
		}
		
		if(Objects.isNull(user)) {
			throw new ObjectNotFoundException(String.format("%s not found", username));
		}
		return user;
	}
	
	@Override
	public User getUserByEmail(String email) {

		User user = null;
		
		for(UserDatabase userDatabase : userDatabases.values()) {
			try {
				user = userDatabase.getUser(email);
				if(Objects.nonNull(user)) {
					break;
				}
			} catch(ObjectNotFoundException e) { }
		}
		
		if(Objects.isNull(user)) {
			throw new ObjectNotFoundException(String.format("%s not found", email));
		}
		return user;
	}

	@Override
	public void setPassword(User user, char[] newPassword, boolean passwordChangeRequired) {
		
		permissionService.assertPermission(SET_PASSWORD_PERMISSION);
		assertCapability(user, UserDatabaseCapabilities.MODIFY_PASSWORD);
		getDatabase(user).setPassword(user, newPassword, passwordChangeRequired);
		
	}
	
	@Override
	public void changePassword(User user, char[] oldPassword, char[] newPassword) {
		
		permissionService.assertPermission(CHANGE_PASSWORD_PERMISSION);
		assertCapability(user, UserDatabaseCapabilities.MODIFY_PASSWORD);
		verifyPassword(user, oldPassword);
		getDatabase(user).setPassword(user, newPassword, false);
		
	}
	
	@Override
	public void changePassword(User user, char[] newPassword, boolean passwordChangeRequired) {
		
		permissionService.assertPermission(CHANGE_PASSWORD_PERMISSION);
		assertCapability(user, UserDatabaseCapabilities.MODIFY_PASSWORD);
		getDatabase(user).setPassword(user, newPassword, passwordChangeRequired);
		
	}

	@Override
	public void initializeSystem(boolean newSchema) {

		loadUserDatabases();
		
		permissionService.registerCustomPermission(CHANGE_PASSWORD_PERMISSION);
		permissionService.registerCustomPermission(SET_PASSWORD_PERMISSION);
		
	}
	
	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {

	}

	@Override
	public Iterable<User> allUsers() {
		
		CompoundIterable<User> iterator = new CompoundIterable<>();
		userDatabases.forEach((k,v)->{
			iterator.add(v.iterateUsers());
		});
		return iterator;
	}

	@Override
	public Collection<ObjectTemplate> getCreateUserTemplates() {
		
		List<ObjectTemplate> templates = new ArrayList<>();
		userDatabases.forEach((k,v)->{
			if(v.getCapabilities().contains(UserDatabaseCapabilities.CREATE)){
				templates.add(v.getUserTemplate());
			}
		});
		return templates;
	}

	@Override
	public void deleteUser(User user) {
		
		assertWrite(USER_RESOURCE_KEY);
		assertCapability(user, UserDatabaseCapabilities.DELETE);
		
		for(UserAware ua : applicationService.getBeans(UserAware.class)) {
			ua.onDeleteUser(user);
		}
		
		getDatabase(user).deleteUser(user);
	}

	@Override
	public void updateUser(User user) {
		assertWrite(USER_RESOURCE_KEY);
		assertCapability(user, UserDatabaseCapabilities.UPDATE);
		getDatabase(user).updateUser(user);
	}

	@Override
	public void createUser(User user, char[] password, boolean forceChange) {
		assertWrite(USER_RESOURCE_KEY);
		assertCapability(user, UserDatabaseCapabilities.CREATE);
		getDatabase(user).createUser(user, password, forceChange);
	}

	protected void assertCapability(User user, UserDatabaseCapabilities capability) {
		if(!getDatabase(user).getCapabilities().contains(capability)) {
			throw new AccessDeniedException(String.format("User database does not support %s", capability.name()));
		}
	}
	
	@Override
	public boolean supportsLogin(User user) {
		return getDatabase(user).getCapabilities().contains(UserDatabaseCapabilities.LOGON);
	}

	@Override
	public Map<String, String> getUserProperties(User user) {
		// TODO Return from user database implementation
		return new HashMap<>();
	}

}

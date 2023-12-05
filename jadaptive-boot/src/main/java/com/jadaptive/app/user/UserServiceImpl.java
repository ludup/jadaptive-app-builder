package com.jadaptive.app.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.stats.ResourceService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.FakeUser;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserAware;
import com.jadaptive.api.user.UserDatabase;
import com.jadaptive.api.user.UserDatabaseCapabilities;
import com.jadaptive.api.user.UserService;
import com.jadaptive.utils.Utils;

@Service
public class UserServiceImpl extends AbstractUUIDObjectServceImpl<User> implements UserService, ResourceService, TenantAware, UUIDObjectService<User> {
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private EventService eventService; 
	
	private Map<Class<? extends User>,UserDatabase> userDatabases = new HashMap<>();
	
	@Autowired
	private TenantAwareObjectDatabase<User> userRepository;

	@Override
	public Integer getOrder() {
		return 0;
	}
	
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
		
		User user = userRepository.get(uuid, User.class);
		
		if(Objects.isNull(user)) {
			throw new ObjectNotFoundException(String.format("User with id %s not found", uuid));
		}
		return user;
	}

	@Override
	public boolean verifyPassword(User user, char[] password) {
		try {
			return !(user instanceof FakeUser) && getDatabase(user).verifyPassword(user, password);
		} catch(ObjectNotFoundException e) {
			return false;
		}
	}


	@Override
	public User getUser(String username) {

		try {
			return userRepository.get(User.class, 
					SearchField.or(
					SearchField.eq("username", username),
					SearchField.in("aliases", username)));
		} catch(ObjectNotFoundException e) {
			for(UserDatabase userDatabase : applicationService.getBeans(UserDatabase.class)) {
				if(userDatabase.getCapabilities().contains(UserDatabaseCapabilities.IMPORT)) {
					try {
						User user = userDatabase.importUser(username);
						if(Objects.nonNull(user)) {
							if(StringUtils.isBlank(user.getUuid())) {
								user.setUuid(UUID.randomUUID().toString());
							}
							return user;
						}
					} catch(ObjectNotFoundException e2) { }
				}
			}
			
			throw new ObjectNotFoundException(String.format("%s not found", username));
		
		}
	}

	@Override
	public void setPassword(User user, char[] newPassword, boolean passwordChangeRequired) {
		
		permissionService.assertPermission(SET_PASSWORD_PERMISSION);
		assertCapability(user, UserDatabaseCapabilities.MODIFY_PASSWORD);
		
		try {
			getDatabase(user).setPassword(user, newPassword, passwordChangeRequired);
			eventService.publishEvent(new SetPasswordEvent(user));
		} catch(Throwable e) {
			eventService.publishEvent(new SetPasswordEvent(user, e));
			throw e;
		}
	}
	
	@Override
	public void changePassword(User user, char[] oldPassword, char[] newPassword) {
		
		permissionService.assertPermission(CHANGE_PASSWORD_PERMISSION);
		assertCapability(user, UserDatabaseCapabilities.MODIFY_PASSWORD);
		
		try {
			verifyPassword(user, oldPassword);
			getDatabase(user).setPassword(user, newPassword, false);
			eventService.publishEvent(new ChangePasswordEvent());
		} catch(Throwable e) {
			eventService.publishEvent(new ChangePasswordEvent(e));
		}
		
	}
	
	@Override
	public void changePassword(User user, char[] newPassword, boolean passwordChangeRequired) {
		
		permissionService.assertPermission(CHANGE_PASSWORD_PERMISSION);
		assertCapability(user, UserDatabaseCapabilities.MODIFY_PASSWORD);
		
		try {
			getDatabase(user).setPassword(user, newPassword, passwordChangeRequired);
			eventService.publishEvent(new ChangePasswordEvent());
		} catch(Throwable e) {
			eventService.publishEvent(new ChangePasswordEvent(e));
			throw e;
		}
		
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
		return user instanceof FakeUser || getDatabase(user).getCapabilities().contains(UserDatabaseCapabilities.LOGON);
	}

	@Override
	public String saveOrUpdate(User user) {
		UserDatabase db = getDatabase(user);
		boolean isNew = StringUtils.isBlank(user.getUuid());
		db.updateUser(user);
		if(user instanceof PasswordEnabledUser) {
			if(isNew || !db.hasPassword(user)) {
				throw new UriRedirect("/app/ui/set-password/" + user.getUuid());
			}
		}

		return user.getUuid();
	}

	@Override
	public long getTotalResources() {
		return userRepository.count(User.class);
	}

	@Override
	public String getResourceKey() {
		return "users";
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Collection<User> getUsersByUUID(Collection<String> users) {
		
		List<User> tmp = new ArrayList<>();
		for(User user : userRepository.list(User.class, SearchField.in("uuid", users))) {
			tmp.add(user);
		}
		return tmp;
	}

	@Override
	public long countUsers() {
		return userRepository.count(User.class);
	}

	@Override
	protected Class<User> getResourceClass() {
		return User.class;
	}

	@Override
	public Map<String, String> getUserProperties(User user) {
		return new HashMap<>();
	}

	@Override
	public void registerLogin(User user) {
		
		eventService.haltEvents();
		user.setLastLogin(Utils.now());
		getDatabase(user).registerLogin(user);
		eventService.resumeEvents();
	}

	@Override
	public Iterable<User> allObjects(String userTemplate) {
		return userRepository.list(User.class, SearchField.eq("resourceKey", userTemplate));
	}

}

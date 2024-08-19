package com.jadaptive.app.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.avatar.Avatar;
import com.jadaptive.api.avatar.AvatarRequest;
import com.jadaptive.api.avatar.AvatarService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.AbstractObject;
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
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.ChangePasswordEvent;
import com.jadaptive.api.user.FakeUser;
import com.jadaptive.api.user.PasswordEnabledUser;
import com.jadaptive.api.user.SetPasswordEvent;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserAware;
import com.jadaptive.api.user.UserDatabase;
import com.jadaptive.api.user.UserDatabaseCapabilities;
import com.jadaptive.api.user.UserService;
import com.jadaptive.api.user.VerifyPasswordEvent;
import com.jadaptive.utils.Utils;

@Service
public class UserServiceImpl extends AbstractUUIDObjectServceImpl<User> implements UserService, ResourceService, TenantAware, UUIDObjectService<User> {

	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ApplicationService applicationService; 
	
	@Autowired
	private EventService eventService; 
	
	@Autowired
	private AvatarService avatarService; 
	
	private Map<Class<? extends User>,UserDatabase> userDatabases = new HashMap<>();
	
	@Autowired
	private TenantAwareObjectDatabase<User> userRepository;
	
	@Autowired
	private TenantService tenantService; 
	
	private long cachedAllTenantsCount = -1;

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

		for(UserDatabase userDatabase : applicationService.getBeans(UserDatabase.class)) {
			try {
				return userDatabase.findUser(username);
			} catch(ObjectNotFoundException e) { }
		}

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

	@Override
	public void setPassword(User user, char[] newPassword, boolean passwordChangeRequired) {
		
		permissionService.assertPermission(SET_PASSWORD_PERMISSION);
		assertCapability(user, UserDatabaseCapabilities.MODIFY_PASSWORD);
		
		try {
			eventService.publishEvent(new VerifyPasswordEvent(user, newPassword));
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
			eventService.publishEvent(new VerifyPasswordEvent(user, newPassword));
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
			eventService.publishEvent(new VerifyPasswordEvent(user, newPassword));
			getDatabase(user).setPassword(user, newPassword, passwordChangeRequired);
			eventService.publishEvent(new ChangePasswordEvent());
		} catch(Throwable e) {
			eventService.publishEvent(new ChangePasswordEvent(e));
			throw e;
		}
		
	}
	
	@Override
	public synchronized long allTenantsCount() {
		
		if(cachedAllTenantsCount < 0) {
			long count = 0;
			for(Tenant tenant : tenantService.allObjects()) {
				tenantService.setCurrentTenant(tenant);
				try {
					count += countUsers();
				} finally {
					tenantService.clearCurrentTenant();
				}
			}
			cachedAllTenantsCount = count;
		}
		
		return cachedAllTenantsCount;
	}

	@Override
	public void initializeSystem(boolean newSchema) {

		loadUserDatabases();
		
		permissionService.registerCustomPermission(CHANGE_PASSWORD_PERMISSION);
		permissionService.registerCustomPermission(SET_PASSWORD_PERMISSION);
		
		eventService.created(User.class, (e)->{
			synchronized(UserServiceImpl.this) {
				cachedAllTenantsCount++;
				if(log.isInfoEnabled()) {
					log.info("REMOVEME: Increasing licensed user count to {}", cachedAllTenantsCount);
				}
			}
			
		});
		
		eventService.deleted(User.class, (e)->{
			synchronized(UserServiceImpl.this) {
				cachedAllTenantsCount--;
				if(log.isInfoEnabled()) {
					log.info("REMOVEME: Reducing licensed user count to {}", cachedAllTenantsCount);
				}
			}
		});
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

	@Override
	public Optional<Avatar> find(AvatarRequest request) {
		try {
			var user = request.user().orElseGet(() -> 
				request.id().map(id -> getUserByUUID(id)).orElseGet(() ->
					request.id().map(id -> getUser(id)).orElseThrow(() -> new IllegalArgumentException("Not enough detail to lookup user."))
				)
			);
			if(StringUtils.isNotBlank(user.getAvatar())) {
				return Optional.of(() -> Html.img(user.getAvatar()).addClass("user-avatar-image"));
			}
		}
		catch(ObjectNotFoundException | IllegalArgumentException iae) {
		}
		return Optional.empty();
	}


	@Override
	public Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate) {
		switch(column) {
		case "avatar":
		{
			var user = getObjectByUUID(obj.getUuid());

			var el = new Element("div");
			el.addClass("users-user-avatar");
			el.appendChild(avatarService.avatar(new AvatarRequest.Builder().
					forUser(user).
					build()).render());
			return el;
		}
		default:
			throw new UnsupportedOperationException(column + " is not a known dynamic column!");
		}			
	}
}

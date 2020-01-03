package com.jadaptive.app.permissions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.utils.Utils;

@Service
public class PermissionServiceImpl extends AbstractLoggingServiceImpl implements PermissionService {

	public static final String READ = "read";
	public static final String READ_WRITE = "readWrite";
	
	@Autowired
	TenantService tenantService; 
	
	@Autowired
	RoleService roleService; 
	
	Map<Tenant,Set<String>> tenantPermissions = new HashMap<>();
	
	ThreadLocal<Stack<User>> currentUser = new ThreadLocal<>();
	
	final static String SYSTEM_USER_UUID = "aa53c0f8-cbbe-44be-bb61-cc7bbe6d6f3d";
	
	final static User SYSTEM_USER = new User() {

		@Override
		public String getUuid() {
			return SYSTEM_USER_UUID;
		}

		@Override
		public String getUsername() {
			return "system";
		}

		@Override
		public String getName() {
			return "System";
		}
	};
	
	@Override
	public void registerStandardPermissions(String resourceKey) {
		
		registerPermission(getReadPermission(resourceKey));
		registerPermission(getReadWritePermission(resourceKey));
	}
	
	@Override
	public void setupUserContext(User user) {
		
		if(log.isInfoEnabled()) {
			log.info(String.format("Creating user context for %s", user.getUsername()));
		}
		
		Stack<User> userStack = currentUser.get();
		if(Objects.isNull(userStack)) {
			userStack = new Stack<>();
		}
		userStack.push(user);
		currentUser.set(userStack);
	}
	
	@Override
	public User getCurrentUser() {
		Stack<User> userStack = currentUser.get();
		if(Objects.isNull(userStack) || userStack.isEmpty()) {
			throw new IllegalStateException("There is no user context setup on this thread!");
		}
		return userStack.peek();
	}
	
	@Override
	public void setupSystemContext() {
		setupUserContext(SYSTEM_USER);
	}
	
	@Override
	public void clearUserContext() {
		
		Stack<User> userStack = currentUser.get();
		if(Objects.isNull(userStack) || userStack.isEmpty()) {
			throw new IllegalStateException("There is no user context to clear on this thread!");
		}
		User user = userStack.pop();
		if(log.isInfoEnabled()) {
			log.info(String.format("Cleared user context for %s", user.getUsername()));
		}
	}
	
	private synchronized void registerPermission(String permission) {
		
		Tenant tenant = tenantService.getCurrentTenant();	
		
		if(log.isInfoEnabled()) {
			log.info("Registering permission {} for tenant {}", permission, tenant.getHostname());
		}

		Set<String> allPermissions = tenantPermissions.get(tenant);
		if(Objects.isNull(allPermissions)) {
			allPermissions = new TreeSet<>();
			tenantPermissions.put(tenant, allPermissions);
		}
		
		if(allPermissions.contains(permission)) {
			throw new IllegalArgumentException(String.format("%s is already a registered permission"));
		}
		
		allPermissions.add(permission);
	}
	
	@Override
	public void registerCustomPermission(String customPermission) {
		
		registerPermission(customPermission);
	}
	
	@Override
	public Collection<String> getAllPermissions() {
		return Collections.unmodifiableCollection(getAllPermissions(tenantService.getCurrentTenant()));
	}
	
	@Override
	public Collection<String> getAllPermissions(Tenant tenant) {
		return Collections.unmodifiableCollection(tenantPermissions.get(tenant));
	}
	
	@Override
	public void assertRead(String resourceKey) throws AccessDeniedException {
		assertAnyPermission(getReadPermission(resourceKey), getReadWritePermission(resourceKey));
	}
	
	private String getReadWritePermission(String resourceKey) {
		return String.format("%s.%s", resourceKey, READ_WRITE);
	}

	private String getReadPermission(String resourceKey) {
		return String.format("%s.%s", resourceKey, READ);
	}

	@Override 
	public void assertReadWrite(String resourceKey) throws AccessDeniedException {
		assertAnyPermission(getReadWritePermission(resourceKey));
	}
	
	@Override
	public void assertAnyPermission(String... permissions) throws AccessDeniedException {
		User user = getCurrentUser();
		
		if(log.isDebugEnabled()) {
			log.debug(String.format("Asserting permissions %s for user %s", Utils.csv(permissions), user.getUsername()));
		}
		
		if(user.getUuid().equals(SYSTEM_USER_UUID)) {
			return;
		}
		
		Set<String> allPermissions = new TreeSet<>();
		Collection<Role> roles = roleService.getRoles(user);
		for(Role role : roles) {
			if(role.isAllPermissions()) {
				return;
			}
			allPermissions.addAll(role.getPermissions());
			for(String permission : permissions) {
				if(allPermissions.contains(permission)) {
					return;
				}
			}
		}
		
		if(log.isInfoEnabled()) {
			log.info("User {} denied permission from permission set {}", 
					user.getUsername(), 
					Utils.csv(allPermissions));
		}
		throw new AccessDeniedException();
	}
	
	@Override
	public void assertAllPermission(String... permissions) throws AccessDeniedException {
		User user = getCurrentUser();
		
		if(log.isDebugEnabled()) {
			log.debug(String.format("Asserting all permissions %s for user %s", Utils.csv(permissions), user.getUsername()));
		}
		
		if(user.getUuid().equals(SYSTEM_USER_UUID)) {
			return;
		}
		
		Set<String> allPermissions = new TreeSet<>();
		Collection<Role> roles = roleService.getRoles(user);
		for(Role role : roles) {
			if(role.isAllPermissions()) {
				return;
			}
			allPermissions.addAll(role.getPermissions());
		}
		
		for(String permission : permissions) {
			if(!allPermissions.contains(permission)) {
				if(log.isInfoEnabled()) {
					log.info("User {} denied permission from permission set {}", 
							user.getUsername(), 
							Utils.csv(allPermissions));
				}
				throw new AccessDeniedException();
			}
		}
		
	}
	
	@Override
	public void assertPermission(String permission) throws AccessDeniedException {
		assertAnyPermission(permission);
	}

	@Override
	public boolean isValidPermission(String permission) {
		
		Tenant tenant = tenantService.getCurrentTenant();
		Set<String> allPermissions = tenantPermissions.get(tenant);
		return allPermissions.contains(permission);
	}

	@Override
	public boolean hasUserContext() {

		Stack<User>  users = currentUser.get();
		if(Objects.nonNull(users)) {
			return !users.isEmpty();
		}
		return false;
	}

}
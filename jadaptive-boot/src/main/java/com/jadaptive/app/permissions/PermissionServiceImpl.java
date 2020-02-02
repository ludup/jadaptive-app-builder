package com.jadaptive.app.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.utils.Utils;

@Service
public class PermissionServiceImpl extends AbstractLoggingServiceImpl implements PermissionService, TenantAware {

	public static final String READ = "read";
	public static final String READ_WRITE = "readWrite";
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private RoleService roleService; 
	
	Map<Tenant,Set<String>> tenantPermissions = new HashMap<>();
	Map<Tenant,Map<String,Set<String>>> tenantPermissionsAlias = new HashMap<>();
	
	Set<String> systemPermissions = new TreeSet<>();
	Map<String,Set<String>> systemPermissionsAlias = new HashMap<>();
	
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
		registerPermission(getReadWritePermission(resourceKey), getReadPermission(resourceKey));
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
	
	private synchronized void registerPermission(String permission, String... aliases) {
		
		Tenant tenant = tenantService.getCurrentTenant();	
		
		if(tenant.getSystem()) {
			
			doRegisterPermission(systemPermissions, systemPermissionsAlias, permission, aliases);
		} else {
			
			Set<String> allPermissions = tenantPermissions.get(tenant);
			if(Objects.isNull(allPermissions)) {
				allPermissions = new TreeSet<>();
				tenantPermissions.put(tenant, allPermissions);
			}
			
			Map<String,Set<String>> aliasPermissions = tenantPermissionsAlias.get(tenant);	
			
			if(Objects.isNull(aliasPermissions)) {
				aliasPermissions = new HashMap<>();
				tenantPermissionsAlias.put(tenant, aliasPermissions);
			}
			
			doRegisterPermission(allPermissions, aliasPermissions, permission, aliases);
		}
	}
	
	private synchronized void doRegisterPermission(Set<String> allPermissions, Map<String,Set<String>> aliasPermissions, String permission, String... aliases) {
		
		
		if(log.isInfoEnabled()) {
			log.info("Registering permission {} for tenant {}", permission, tenantService.getCurrentTenant().getHostname());
		}
		
		if(allPermissions.contains(permission)) {
			throw new IllegalArgumentException(String.format("%s is already a registered permission"));
		}
		
		allPermissions.add(permission);
		
		if(aliases.length > 0) {
			if(!aliasPermissions.containsKey(permission)) {
				aliasPermissions.put(permission, new HashSet<>());
			}
			aliasPermissions.get(permission).addAll(Arrays.asList(aliases));
		}
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
		List<String> tmp = new ArrayList<>();
		tmp.addAll(systemPermissions);
		if(tenantPermissions.containsKey(tenant)) {
			tmp.addAll(tenantPermissions.get(tenant));
		}
		return Collections.unmodifiableCollection(tmp);
	}
	
	@Override
	public void assertRead(String resourceKey) throws AccessDeniedException {
		assertAnyPermission(getReadPermission(resourceKey));
	}
	
	@Override
	public String getReadWritePermission(String resourceKey) {
		return String.format("%s.%s", resourceKey, READ_WRITE);
	}

	@Override
	public String getReadPermission(String resourceKey) {
		return String.format("%s.%s", resourceKey, READ);
	}

	@Override 
	public void assertReadWrite(String resourceKey) throws AccessDeniedException {
		assertAnyPermission(getReadWritePermission(resourceKey));
	}
	
	@Override
	public Set<String> resolveCurrentPermissions() {
		return resolvePermissions(getCurrentUser());
	}
	
	@Override
	public Set<String> resolvePermissions(User user) {
		
		Tenant tenant = tenantService.getCurrentTenant();
		Set<String> allPermissions = new TreeSet<>();
		Collection<Role> roles = roleService.getRoles(user);
		for(Role role : roles) {
			if(role.isAllPermissions()) {
				allPermissions.addAll(getAllPermissions());
			}
			allPermissions.addAll(role.getPermissions());
		}

		Set<String> resolvedPermissions = new HashSet<>(allPermissions);
		processAliases(systemPermissionsAlias, allPermissions, resolvedPermissions);
		if(!tenant.getSystem()) {
			processAliases(tenantPermissionsAlias.get(tenant), allPermissions, resolvedPermissions);
		}		
		return resolvedPermissions;
	}
	
	private void processAliases(Map<String,Set<String>> aliasPermissions, Set<String> allPermissions, Set<String> resolvedPermissions) {

		
		for(String permission : allPermissions) {
			Set<String> aliases = aliasPermissions.get(permission);
			if(Objects.nonNull(aliases)) {
				resolvedPermissions.addAll(aliases);
			}
		}
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

		Set<String> resolvedPermissions = resolvePermissions(user);
		Set<String> testPermissions = new HashSet<>(Arrays.asList(permissions));

		testPermissions.retainAll(resolvedPermissions);
		
		if(!testPermissions.isEmpty()) {
			return;
		}
		
		if(log.isInfoEnabled()) {
			log.info("User {} denied permission from permission set {}", 
					user.getUsername(), 
					Utils.csv(resolvedPermissions));
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

		Set<String> resolvedPermissions = resolvePermissions(user);
		
		for(String permission : permissions) {
			if(!resolvedPermissions.contains(permission)) {
				if(log.isInfoEnabled()) {
					log.info("User {} denied permission from permission set {}", 
							user.getUsername(), 
							Utils.csv(resolvedPermissions));
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

	@Override
	public void initializeTenant(Tenant tenant) {
	
	}
	
	@Override
	public void initializeSystem() {
			
//		for(PluginWrapper w : pluginManager.getPlugins()) {
//
//			if(log.isInfoEnabled()) {
//				log.info("Scanning plugin {} for custom permissions in {}", 
//						w.getPluginId(),
//						w.getPlugin().getClass().getPackage().getName());
//			}
//			
//			try {
//				ConfigurationBuilder builder = new ConfigurationBuilder();
//				
//				builder.addClassLoaders(w.getPluginClassLoader());
//				builder.addUrls(ClasspathHelper.forPackage(
//						w.getPlugin().getClass().getPackage().getName(),
//						w.getPluginClassLoader()));
//				builder.addScanners(new TypeAnnotationsScanner());
//
//				Reflections reflections = new Reflections(builder);
//				
//				for(Class<?> clz : reflections.getTypesAnnotatedWith(Permissions.class)) {
//					if(log.isInfoEnabled()) {
//						log.info("Found annotated permissions {}", clz.getName());
//					}
//					Permissions perms = clz.getAnnotation(Permissions.class);
//					for(String key : perms.keys()) {
//						registerCustomPermission(key);
//					}
//				}
//			} catch (Exception e) {
//				log.error("Failed to process annotated templates for plugin {}", w.getPluginId(), e);
//			}
//		}
		
	}
}

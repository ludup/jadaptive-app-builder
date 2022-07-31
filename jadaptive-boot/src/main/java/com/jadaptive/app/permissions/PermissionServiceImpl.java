package com.jadaptive.app.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.PropertyService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.permissions.PermissionUtils;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.NamePairValue;
import com.jadaptive.api.user.User;
import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.app.user.AdminUser;
import com.jadaptive.utils.Utils;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

@Service
public class PermissionServiceImpl extends AbstractLoggingServiceImpl implements PermissionService, TenantAware {


	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private PluginManager pluginManager;
	
	@Autowired
	private PropertyService propertyService;
	
	Set<String> systemPermissions = new TreeSet<>();
	Set<NamePairValue> systemPermissionObjects = new TreeSet<>();
	Map<String,Set<String>> systemPermissionsAlias = new HashMap<>();
	
	ThreadLocal<Stack<User>> currentUser = new ThreadLocal<>();
	
	final static String SYSTEM_USER_UUID = "aa53c0f8-cbbe-44be-bb61-cc7bbe6d6f3d";
	
	final static User SYSTEM_USER = new User() {

		private static final long serialVersionUID = 900617280859406080L;

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

		@Override
		public void setName(String value) {
		}
		
		public void setUuid(String uuid) {
		}
		
		public Boolean isSystem() {
			return true;
		}
		
		public Boolean isHidden() {
			return true;
		}
		
		@Override
		public String getResourceKey() {
			return "user";
		}

	};

	@Override
	public void registerStandardPermissions(String resourceKey) {
		
		registerPermission(PermissionUtils.getReadPermission(resourceKey));
		registerPermission(PermissionUtils.getReadWritePermission(resourceKey), PermissionUtils.getReadPermission(resourceKey));
	}
	
	@Override
	public void setupUserContext(User user) {
		
		if(log.isDebugEnabled()) {
			log.debug(String.format("Creating user context for %s", user.getUsername()));
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
			if(log.isErrorEnabled()) {
				log.error("Calling getCurrentUser without having previously setup a user context on the current thread!!!!");
			}
			throw new AccessDeniedException("There is no user context setup on this thread!");
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
			if(log.isErrorEnabled()) {
				log.error("Calling clearUserContext without having previously setup a user context on the current thread!!!!");
			}
			throw new IllegalStateException("There is no user context to clear on this thread!");
		}
		User user = userStack.pop();
		if(log.isDebugEnabled()) {
			log.debug(String.format("Cleared user context for %s", user.getUsername()));
		}
	}
	
	@Override
	public boolean isAdministrator(User user) {
		Collection<Role> roles = roleService.getRoles(user);
		for(Role role : roles) {
			if(role.isAllPermissions()) {
				return true;
			}
		}
		return false;
	}
	
	private synchronized void registerPermission(String permission, String... aliases) {
		
		Tenant tenant = tenantService.getCurrentTenant();	
		
		if(tenant.isSystem()) {
			doRegisterPermission(systemPermissions, systemPermissionObjects, systemPermissionsAlias, permission, aliases);
		} 
	}
	
	private synchronized void doRegisterPermission(Set<String> allPermissions, Set<NamePairValue> objectPermissions, Map<String,Set<String>> aliasPermissions, String permission, String... aliases) {
		
		
		if(log.isInfoEnabled()) {
			log.info("Registering permission {} for tenant {}", permission, tenantService.getCurrentTenant().getDomain());
		}
		
		if(allPermissions.contains(permission)) {
			throw new IllegalArgumentException(String.format("%s is already a registered permission", permission));
		}
		
		allPermissions.add(permission);
		objectPermissions.add(new NamePairValue(permission, permission));
		
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
	public Set<String> getAllPermissions(Tenant tenant) {
		Set<String> tmp = new TreeSet<>();
		tmp.addAll(systemPermissions);
		return Collections.unmodifiableSet(tmp);
	}
	
	@Override
	public void assertRead(String resourceKey) throws AccessDeniedException {
		assertAnyPermission(PermissionUtils.getReadPermission(resourceKey));
	}

	@Override 
	public void assertReadWrite(String resourceKey) throws AccessDeniedException {
		assertAnyPermission(PermissionUtils.getReadWritePermission(resourceKey));
	}
	
	@Override
	public Set<String> resolveCurrentPermissions() {
		return resolvePermissions(getCurrentUser());
	}
	
	@Override
	public Set<String> resolvePermissions(User user) {
		
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
		
		if(user.getUuid().equals(SYSTEM_USER_UUID) || user instanceof AdminUser) {
			return;
		}

		Set<String> resolvedPermissions = resolvePermissions(user);
		Set<String> testPermissions = new HashSet<>(Arrays.asList(permissions));

		testPermissions.retainAll(resolvedPermissions);
		
		if(!testPermissions.isEmpty()) {
			return;
		}
		
		if(log.isWarnEnabled()) {
			log.debug("User {} denied permission from permission set {}", 
					user.getUsername(), 
					StringUtils.defaultIfBlank(Utils.csv(resolvedPermissions), "<empty>"));
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
				if(log.isWarnEnabled()) {
					log.warn("User {} denied permission from permission set {}", 
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
		Set<String> allPermissions = getAllPermissions(tenant);
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
	public void initializeTenant(Tenant tenant, boolean newSchema) {
		
		for(PluginWrapper w : pluginManager.getPlugins()) {

			if(log.isInfoEnabled()) {
				log.info("Scanning plugin {} for custom permissions in {}", 
						w.getPluginId(),
						w.getPlugin().getClass().getPackage().getName());
			}
			
			scanForPermissions(w.getPluginClassLoader(), w.getPlugin().getClass().getPackage().getName(), newSchema);
		}
		
		scanForPermissions(getClass().getClassLoader(), "com.jadaptive.app", newSchema);
	}
	
	@Override
	public void initializeSystem(boolean newSchema) {
		initializeTenant(tenantService.getSystemTenant(), newSchema);
	}

	private void scanForPermissions(ClassLoader classloader, String name, boolean newSchema) {
		
		try {
			try (ScanResult scanResult =
                    new ClassGraph()                 
                        .enableAllInfo()  
                        .addClassLoader(classloader)
                        .whitelistPackages(name)   
                        .scan()) {                  
                for (ClassInfo clz : scanResult.getClassesWithAnnotation(Permissions.class.getName())) {
					if(log.isInfoEnabled()) {
						log.info("Found annotated permissions {}", clz.getName());
					}
					Permissions perms = clz.loadClass().getAnnotation(Permissions.class);
					for(String key : perms.keys()) {
						registerCustomPermission(key);
					}
					
					Role everyoneRole = roleService.getEveryoneRole();
					
					for(String permission : perms.defaultPermissions()) {
						if(!propertyService.getBoolean(String.format("defaultPermission.%s.%s", clz.getName(), permission), false)) {
							if(!everyoneRole.getPermissions().contains(permission)) {
								roleService.grantPermission(everyoneRole, permission);
							}
							propertyService.setBoolean(String.format("defaultPermission.%s.%s", clz.getName(), permission), true);
						}
					}
	
                }
            }

		} catch (Exception e) {
			log.error("Failed to process annotated templates", e);
		}
	}

	@Override
	public void assertAdministrator() {
		if(!isAdministrator(getCurrentUser())) {
			if(log.isWarnEnabled()) {
				log.warn("User {} denied administrative access", 
						getCurrentUser().getUsername());
			}
			throw new AccessDeniedException(String.format("%s is not an Administrator", 
					getCurrentUser().getName()));
		}
	}

	@Override
	public Collection<NamePairValue> getPermissions() {

		List<NamePairValue> tmp = new ArrayList<>();
		tmp.addAll(systemPermissionObjects);

		Collections.sort(tmp, new Comparator<NamePairValue>() {

			@Override
			public int compare(NamePairValue o1, NamePairValue o2) {
				return	o1.getName().compareTo(o2.getName());
			}
		});
		
		return Collections.unmodifiableCollection(tmp);
	}
}

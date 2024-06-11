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
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.PropertyService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.AuthenticatedContext;
import com.jadaptive.api.permissions.OwnershipService;
import com.jadaptive.api.permissions.OwnershipServiceBean;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.permissions.PermissionUtils;
import com.jadaptive.api.permissions.Permissions;
import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDObjectService;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.NamePairValue;
import com.jadaptive.api.ui.Redirect;
import com.jadaptive.api.user.User;
import com.jadaptive.app.AbstractLoggingServiceImpl;
import com.jadaptive.utils.Utils;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

@Service
@Aspect
public class PermissionServiceImpl extends AbstractLoggingServiceImpl implements PermissionService, TenantAware {


	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private PluginManager pluginManager;
	
	@Autowired
	private PropertyService propertyService;
	
	@Autowired
	private ApplicationService applicationService;
	
	@Autowired
	private ObjectService objectService;
	
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
		
		@Override
		public String getResourceKey() {
			return "user";
		}

	};

	@Around("@annotation(com.jadaptive.api.permissions.AuthenticatedContext)")
	public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
		var sig = pjp.getSignature();
		if (sig instanceof MethodSignature) {
			var ms = (MethodSignature) sig;
			var m = ms.getMethod();
			var annot = m.getAnnotation(AuthenticatedContext.class);
			if (annot == null) {
				/**
				 * INFO BPS 30/07/22 - Check the implementation class for annotation too. AOP
				 * gives us the interface method above, but the implementation can be annotated
				 * too. So we look for a method with the same name and signature.
				 */
				var clz = pjp.getTarget().getClass();
				try {
					var mth = clz.getMethod(m.getName(), m.getParameterTypes());
					annot = mth.getAnnotation(AuthenticatedContext.class);
				} catch (Exception e) {
				}
			}
			if (annot != null) {
				if (annot.preferActive()) {
					try(var c = userContext()) {
						return pjp.proceed();
					} catch (Throwable e) {
						log.error("Exception thrown from user context annotated method", e);
						throw e;
					}
				} else if (annot.system()) {
					try (var c = systemContext()) {
						return pjp.proceed();
					} catch (Throwable e) {
						log.error("Exception thrown from system context annotated method", e);
						throw e;
					}
				} else {
					throw new UnsupportedOperationException("Not supported in services.");
				}
			}
		}
		return pjp.proceed();
	}

	@Override
	public UncheckedCloseable systemContext() {
		setupSystemContext();
		return new UncheckedCloseable() {
			@Override
			public void close() {
				clearUserContext();
			}
		};
	}

	@Override
	public UncheckedCloseable userContext() {
		return userContext(getCurrentUser());
	}
	
	@Override
	public UncheckedCloseable userContext(User user) {
		setupUserContext(user);
		return new UncheckedCloseable() {
			@Override
			public void close() {
				clearUserContext();
			}
		};
	}
	
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
			if(log.isWarnEnabled()) {
				log.warn("Access denied because there is no user in the current context");
			}
			throw new AccessDeniedException();
		}
		return userStack.peek();
	}
	
	@Override
	public void setupSystemContext() {
		setupUserContext(SYSTEM_USER);
	}
	
	@Override
	public User getSystemUser() {
		return SYSTEM_USER;
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
		if(user.equals(SYSTEM_USER)) {
			return true;
		}
		Collection<Role> roles = roleService.getRoles(user);
		for(Role role : roles) {
			if(role.isAllPermissions()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public <T> T as(User user, Callable<T> call) {
		setupUserContext(user);
		try {
			return call.call();
		}  catch(Redirect e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			clearUserContext();
		}
	}
	
	@Override
	public <T> T asSystem(Callable<T> call) {
		setupUserContext(SYSTEM_USER);
		try {
			return call.call();
		}  catch(Redirect e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			clearUserContext();
		}
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
	public void assertWrite(String resourceKey) throws AccessDeniedException {
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
		if(isAdministrator()) {
			allPermissions.addAll(getAllPermissions());
		} else {
			for(Role role : roles) {
				if(role.isAllPermissions()) {
					allPermissions.addAll(getAllPermissions());
				}
				allPermissions.addAll(role.getPermissions());
			}
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
		
		if( isAdministrator(user)) {
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
		
		if(isAdministrator(user)) {
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

	}

	@Override
	public void assertAdministrator() {
		if(!isAdministrator(getCurrentUser())) {
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

	@Override
	public void assertAssignment(AssignableUUIDEntity obj) {
		
		if(obj.getUsers().contains(getCurrentUser())) {
			return;
		}
		if(roleService.hasRole(getCurrentUser(), obj.getRoles())) {
			return;
		}
		throw new AccessDeniedException(String.format("Current user is not assigned to object " + obj.getUuid()));
	}

//	@Override
//	public void assertOwnership(PersonalUUIDEntity obj) {
//		

//	}
//
//	@Override
//	public void assertOwnership(AbstractObject e) {
//		
//		String ownerUUID = e.getValue("ownerUUID").toString();
//		if(Objects.isNull(ownerUUID) || !ownerUUID.equals(getCurrentUser().getUuid())) {
//			throw new AccessDeniedException(String.format("Current user is not own the object " + e.getUuid()));
//		}	
//	}
	
	@Override
	public void assertOwnership(UUIDDocument e) {
		
		if(e instanceof AbstractObject) {
			e = objectService.toUUIDDocument((AbstractObject) e);
		}
		
		if(e instanceof PersonalUUIDEntity) {
			PersonalUUIDEntity obj = (PersonalUUIDEntity) e;
			if(Objects.isNull(obj.getOwnerUUID()) || !obj.getOwnerUUID().equals(getCurrentUser().getUuid())) {
				throw new AccessDeniedException(String.format("Current user is the owner the object " + e.getUuid()));
			}
		} else {
			ObjectServiceBean serviceAnnotation = e.getClass().getAnnotation(ObjectServiceBean.class);
			if(Objects.nonNull(serviceAnnotation)) {
				UUIDObjectService<?> service = applicationService.getBean(serviceAnnotation.bean());
				if(service instanceof OwnershipService) {
					if(((OwnershipService)service).isOwner(e)) {
						return;
					}
				}
			}
			
			OwnershipServiceBean reference = e.getClass().getAnnotation(OwnershipServiceBean.class);
			if(Objects.nonNull(reference)) {
				OwnershipService service = applicationService.getBean(reference.bean());
				if(!service.isOwner(e)) {
					throw new AccessDeniedException(String.format("Current user not owner the object " + e.getUuid()));
				}
			}
		}
	}

//	@Override
//	public void assertRead(ObjectTemplate template) {
//		if(template.getPermissionProtected()) {
//			if(template.hasParent()) {
//				assertRead(templateService.getParentTemplate(template).getResourceKey());
//			} else {
//				assertRead(template.getResourceKey());
//			}
//		}
//	}
//	
//	@Override
//	public void assertWrite(ObjectTemplate template) {
//		if(template.getPermissionProtected()) {
//			if(template.hasParent()) {
//				assertWrite(templateService.getParentTemplate(template).getResourceKey());
//			} else {
//				assertWrite(template.getResourceKey());
//			}
//		}
//	}
}

package com.jadaptive.app.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.auth.AuthenticationService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.AuthenticatedService;
import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantAware;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserAware;
import com.jadaptive.app.user.UserServiceImpl;

@Service
public class RoleServiceImpl extends AuthenticatedService implements RoleService, TenantAware, UserAware {

	private static final String ADMINISTRATOR_UUID = "1bfbaf16-e5af-4825-8f8a-83ce2f5bf81f";
	private static final String EVERYONE_UUID = "c4b54f49-c478-46cc-8cfa-aaebaa4ea50f";
	private static final String EVERYONE = "Everyone";
	private static final String ADMINISTRATION = "Administration";
	
	@Autowired
	private TenantAwareObjectDatabase<Role> repository; 

	@Autowired
	private TenantService tenantService;  
	
	@Override
	public Integer getOrder() {
		return Integer.MIN_VALUE;
	}
	
	@Override
	public void initializeSystem(boolean newSchema) {
		initializeTenant(getCurrentTenant(), newSchema);
	}

	@Override
	public void initializeTenant(Tenant tenant, boolean newSchema) {

		if(newSchema) {
			setupDefaultRoles(tenant);
		}
	}
	
	private void setupDefaultRoles(Tenant tenant) {
		
		Role role = new Role();
		role.setUuid(ADMINISTRATOR_UUID);
		role.setName(ADMINISTRATION);
		role.setSystem(true);
		role.setAllPermissions(true);
		
		repository.saveOrUpdate(role);
		
		role = new Role();
		role.setUuid(EVERYONE_UUID);
		role.setName(EVERYONE);
		role.setSystem(true);
		role.setAllUsers(true);
		
		role.setPermissions(Arrays.asList(UserServiceImpl.CHANGE_PASSWORD_PERMISSION));
		role.setPermissions(Arrays.asList(AuthenticationService.USER_LOGIN_PERMISSION));
		repository.saveOrUpdate(role);
	}


	@Override
	public Role getAdministrationRole() {
		return repository.get(ADMINISTRATOR_UUID, Role.class);
	}
	
	@Override
	public Role getEveryoneRole() {
		return repository.get(EVERYONE_UUID, Role.class);
	}
	
	@Override
	public Collection<Role> getRoles(User user) {
		
		Set<Role> roles = new HashSet<>();
		roles.addAll(getRolesByUser(user));
		roles.addAll(getAllUserRoles());
		return roles;
	}
	
	@Override
	public Role getRoleByName(String name) {
		assertRead(Role.RESOURCE_KEY);
		return repository.get(Role.class, SearchField.eq("name", name));
	}
	
	@Override
	public Role createRole(String roleName, User... users) {
		return createRole(roleName, Arrays.asList(users));
	}
	
	@Override
	public Role createRole(String roleName, Collection<User> users) {
		
		assertWrite(Role.RESOURCE_KEY);
		
		Role role = new Role();
		role.setName(roleName);
		
		repository.saveOrUpdate(role);
		
		doAssign(role, users.toArray(new User[0]));
		
		return role;
	}
	
	@Override
	public void assignRole(Role role, User... users) {
		assertWrite(Role.RESOURCE_KEY);
		doAssign(role, users);
	}
	
	private void doAssign(Role role, User... users) {
		
		if(EVERYONE_UUID.equals(role.getUuid())) {
			throw new ObjectException("You cannot assign a user to the Everyone role");
		}
		
		Set<String> uuids = new HashSet<>();
		for(User user : users) {
			uuids.add(user.getUuid());
		}
		
		role.getUsers().addAll(uuids);
		
		repository.saveOrUpdate(role);
	}
	
	@Override
	public void grantPermission(Role role, String... permissions) {
		
		assertWrite(Role.RESOURCE_KEY);
		
		Set<String> resolved = new HashSet<>();
		for(String permission : permissions) {
			if(!isValidPermission(permission)) {
				throw new ObjectException(String.format("%s is not a valid permission", permission));
			}
			if(role.getPermissions().contains(permission)) {
				throw new ObjectException(String.format("%s is already granted on %s", permission, role.getName()));
			}
			resolved.add(permission);
		}
		
		role.getPermissions().addAll(resolved);
		repository.saveOrUpdate(role);
	}
	
	@Override
	public void revokePermission(Role role, String... permissions) {
		
		assertWrite(Role.RESOURCE_KEY);
		
		Set<String> resolved = new HashSet<>();
		for(String permission : permissions) {
			if(!isValidPermission(permission)) {
				throw new ObjectException(String.format("%s is not a valid permission", permission));
			}
			if(!role.getPermissions().contains(permission)) {
				throw new ObjectException(String.format("%s is not granted on %s", permission, role.getName()));
			}
			resolved.add(permission);
		}
		
		role.getPermissions().removeAll(resolved);
		repository.saveOrUpdate(role);
	}
	
	@Override
	public void unassignRole(Role role, User... users) {
		
		assertWrite(Role.RESOURCE_KEY);
		
		Set<String> uuids = new HashSet<>();
		for(User user : users) {
			uuids.add(user.getUuid());
		}
		
		if(ADMINISTRATOR_UUID.equals(role.getUuid())) {
			
			Set<String> roleUuids = new HashSet<>(role.getUsers());
			roleUuids.removeAll(uuids);
			if(roleUuids.isEmpty() && !tenantService.isSetupMode()) {
				throw new ObjectException("This operation would remove the last user from the Administration role");
			}
		}
		
		if(EVERYONE_UUID.equals(role.getUuid())) {
			throw new ObjectException("You cannot unassign a user from the Everyone role");
		}
		
		role.getUsers().removeAll(uuids);
		
		repository.saveOrUpdate(role);
	}

	@Override
	public boolean hasRole(User user, Collection<Role> roles) {
		Collection<Role> userRoles = getRoles(user);
		return !Collections.disjoint(userRoles, roles);
	}

	@Override
	public boolean hasRole(User user, Role... roles) {
		return hasRole(user, Arrays.asList(roles));
	}


	@Override
	public void deleteRole(Role role) {
		assertWrite(Role.RESOURCE_KEY);
		repository.delete(role);
	}


	@Override
	public Iterable<Role> listRoles() {
		assertRead(Role.RESOURCE_KEY);
		return repository.list(Role.class);
	}


	@Override
	public Role getRoleByUUID(String uuid) {
		return repository.get(uuid, Role.class);
	}


	@Override
	public void onDeleteUser(User user) {
		
		for(Role role : repository.list(Role.class)) {
			if(role.getUsers().contains(user.getUuid())) {
				role.getUsers().remove(user.getUuid());
				repository.saveOrUpdate(role);
			}
		}
		
	}
	
	@Override
	public Collection<Role> getRolesByUser(User user) {
		List<Role> results = new ArrayList<>(getAllUserRoles());
		results.addAll(repository.searchObjects(Role.class, SearchField.in("users", user.getUuid())));
		return results;
	}

	@Override
	public Collection<Role> getAllUserRoles() {
		return repository.searchObjects(Role.class, SearchField.eq("allUsers", true));
	}

	@Override
	public Iterable<Role> allRoles() {
		return repository.list(Role.class);
	}

	@Override
	public boolean isAssigned(AssignableUUIDEntity obj, User user) {
		
		if(obj.getUsers().contains(user.getUuid())) {
			return true;
		}
		
		for(Role role : getRoles(user)) {
			if(obj.getRoles().contains(role.getUuid())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Collection<Role> getRolesByUUID(Collection<String> roles) {
		
		List<Role> tmp = new ArrayList<>();
		for(Role role : repository.list(Role.class, SearchField.in("uuid", roles))) {
			tmp.add(role);
		}
		return tmp;
	}

}

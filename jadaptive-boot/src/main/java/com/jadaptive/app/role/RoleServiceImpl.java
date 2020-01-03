package com.jadaptive.role;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.jadaptive.entity.EntityException;
import com.jadaptive.permissions.PermissionService;
import com.jadaptive.tenant.AbstractTenantAwareObjectDatabase;
import com.jadaptive.tenant.AbstractTenantAwareObjectServiceImpl;
import com.jadaptive.tenant.events.TenantCreatedEvent;
import com.jadaptive.user.User;

@Service
public class RoleServiceImpl extends AbstractTenantAwareObjectServiceImpl<Role> implements RoleService {

	private static final String ADMINISTRATOR_UUID = "1bfbaf16-e5af-4825-8f8a-83ce2f5bf81f";
	private static final String EVERYONE_UUID = "c4b54f49-c478-46cc-8cfa-aaebaa4ea50f";
	private static final String EVERYONE = "Everyone";
	private static final String ADMINISTRATION = "Administration";
	
	@Autowired
	RoleRepository repository; 
	
	@Autowired
	PermissionService permissionService; 
	
	@Override
	public AbstractTenantAwareObjectDatabase<Role> getRepository() {
		return repository;
	}

	@EventListener
	@Override
	public void onTenantCreated(TenantCreatedEvent evt) {
		Role role = new Role();
		role.setUuid(ADMINISTRATOR_UUID);
		role.setName(ADMINISTRATION);
		role.setSystem(true);
		role.setAllPermissions(true);
		
		saveOrUpdate(role);
		
		role = new Role();
		role.setUuid(EVERYONE_UUID);
		role.setName(EVERYONE);
		role.setSystem(true);
		role.setAllUsers(true);
		
		saveOrUpdate(role);
		
	}
	
	@Override
	public Role getAdministrationRole() {
		return get(ADMINISTRATOR_UUID);
	}
	
	@Override
	public Role getEveryoneRole() {
		return get(EVERYONE_UUID);
	}
	
	@Override
	public Collection<Role> getRoles(User user) {
		
		Set<Role> roles = new HashSet<>();
		roles.addAll(repository.matchCollectionObjects("users", user.getUuid()));
		roles.addAll(repository.list("allUsers", "true"));
		return roles;
	}
	
	@Override
	public Role getRoleByName(String name) {
		assertRead();
		return get("name", name);
	}
	
	@Override
	public Role createRole(String roleName, User... users) {
		return createRole(roleName, Arrays.asList(users));
	}
	
	@Override
	public Role createRole(String roleName, Collection<User> users) {
		
		assertReadWrite();
		
		Role role = new Role();
		role.setName(roleName);
		
		saveOrUpdate(role);
		
		for(User user : users) {
			assignRole(role, user);
		}
		
		return role;
	}
	
	@Override
	public void assignRole(Role role, User... users) {
		
		assertReadWrite();
		
		if(EVERYONE_UUID.equals(role.getUuid())) {
			throw new EntityException("You cannot assign a user to the Everyone role");
		}
		
		Set<String> uuids = new HashSet<>();
		for(User user : users) {
			if(role.getUsers().contains(user.getUuid())) {
				throw new EntityException(String.format(
						"%s is already a member of the %s role", user.getUsername(), role.getName()));
			}
			uuids.add(user.getUuid());
		}
		
		role.getUsers().addAll(uuids);
		
		saveOrUpdate(role);
	}
	
	@Override
	public void grantPermission(Role role, String... permissions) {
		
		assertReadWrite();
		
		Set<String> resolved = new HashSet<>();
		for(String permission : permissions) {
			if(!permissionService.isValidPermission(permission)) {
				throw new EntityException(String.format("%s is not a valid permission", permission));
			}
			if(role.getPermissions().contains(permission)) {
				throw new EntityException(String.format("%s is already granted on %s", permission, role.getName()));
			}
			resolved.add(permission);
		}
		
		role.getPermissions().addAll(resolved);
		saveOrUpdate(role);
	}
	
	@Override
	public void revokePermission(Role role, String... permissions) {
		
		assertReadWrite();
		
		Set<String> resolved = new HashSet<>();
		for(String permission : permissions) {
			if(!permissionService.isValidPermission(permission)) {
				throw new EntityException(String.format("%s is not a valid permission", permission));
			}
			if(!role.getPermissions().contains(permission)) {
				throw new EntityException(String.format("%s is not granted on %s", permission, role.getName()));
			}
			resolved.add(permission);
		}
		
		role.getPermissions().removeAll(resolved);
		saveOrUpdate(role);
	}
	
	@Override
	public void unassignRole(Role role, User... users) {
		
		assertReadWrite();
		
		Set<String> uuids = new HashSet<>();
		for(User user : users) {
			uuids.add(user.getUuid());
		}
		
		if(ADMINISTRATOR_UUID.equals(role.getUuid())) {
			
			Set<String> roleUuids = new HashSet<>(role.getUsers());
			roleUuids.removeAll(uuids);
			if(roleUuids.isEmpty()) {
				throw new EntityException("This operation would remove the last user from the Administration role");
			}
		}
		
		if(EVERYONE_UUID.equals(role.getUuid())) {
			throw new EntityException("You cannot unassign a user from the Everyone role");
		}
		
		role.getUsers().removeAll(uuids);
		
		saveOrUpdate(role);
	}
}

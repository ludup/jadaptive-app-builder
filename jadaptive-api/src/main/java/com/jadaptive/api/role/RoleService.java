package com.jadaptive.api.role;

import java.util.Collection;

import com.jadaptive.api.tenant.AbstractTenantAwareObjectService;
import com.jadaptive.api.tenant.events.TenantCreatedEvent;
import com.jadaptive.api.user.User;

public interface RoleService extends AbstractTenantAwareObjectService<Role> {

	void onTenantCreated(TenantCreatedEvent evt);

	Role getAdministrationRole();

	Role getEveryoneRole();

	void assignRole(Role role, User... user);

	void unassignRole(Role role, User... user);

	Collection<Role> getRoles(User user);

	Role getRoleByName(String name);

	Role createRole(String roleName, User... users);
	
	Role createRole(String roleName, Collection<User> users);

	void grantPermission(Role role, String... permissions);

	void revokePermission(Role role, String... permissions);

	boolean hasRole(User currentUser, Collection<Role> roles);

	boolean hasRole(User currentUser, Role... roles);
}

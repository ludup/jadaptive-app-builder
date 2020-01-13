package com.jadaptive.api.role;

import java.util.Collection;

import com.jadaptive.api.tenant.AbstractTenantAwareObjectDatabase;
import com.jadaptive.api.user.User;

public interface RoleRepository extends AbstractTenantAwareObjectDatabase<Role> {

	Collection<Role> getRolesByUser(User user);

	Collection<Role> getAllUserRoles();

}

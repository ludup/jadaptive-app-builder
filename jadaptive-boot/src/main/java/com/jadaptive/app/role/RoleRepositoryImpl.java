package com.jadaptive.app.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleRepository;
import com.jadaptive.api.user.User;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.app.tenant.AbstractTenantAwareObjectDatabaseImpl;

@Repository
public class RoleRepositoryImpl extends AbstractTenantAwareObjectDatabaseImpl<Role> implements RoleRepository {

	protected RoleRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	public Class<Role> getResourceClass() {
		return Role.class;
	}

	@Override
	public Collection<Role> getRolesByUser(User user) {
		List<Role> results = new ArrayList<>(getAllUserRoles());
		results.addAll(searchObjects(SearchField.in("users", user.getUuid())));
		return results;
	}

	@Override
	public Collection<Role> getAllUserRoles() {
		return searchObjects(SearchField.eq("allUsers", true));
	}

}

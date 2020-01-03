package com.jadaptive.app.role;

import org.springframework.stereotype.Repository;

import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleRepository;
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

}

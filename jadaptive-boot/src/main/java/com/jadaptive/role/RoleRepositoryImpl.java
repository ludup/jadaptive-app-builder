package com.jadaptive.role;

import org.springframework.stereotype.Repository;

import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.tenant.AbstractTenantAwareObjectDatabaseImpl;

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

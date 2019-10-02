package com.jadaptive.role;

import java.util.Collection;

import org.springframework.stereotype.Repository;

import com.jadaptive.db.DocumentDatabase;
import com.jadaptive.tenant.AbstractTenantAwareObjectDatabaseImpl;
import com.jadaptive.user.User;

@Repository
public class RoleRepositoryImpl extends AbstractTenantAwareObjectDatabaseImpl<Role> implements RoleRepository {

	protected RoleRepositoryImpl(DocumentDatabase db) {
		super(db);
	}

	@Override
	protected Class<Role> getResourceClass() {
		return Role.class;
	}

}

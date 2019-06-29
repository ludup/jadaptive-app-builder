package com.jadaptive.tenant;

import java.util.Collection;

import org.springframework.stereotype.Repository;

import com.jadaptive.repository.AbstractUUIDRepositoryImpl;
import com.jadaptive.repository.RepositoryException;

@Repository
public class TenantRepositoryImpl extends AbstractUUIDRepositoryImpl<Tenant> implements TenantRepository {

	@Override
	protected Class<Tenant> getResourceClass() {
		return Tenant.class;
	}

	@Override
	public String getName() {
		return "Tenant";
	}

	@Override
	public Tenant createEntity() {
		return new Tenant();
	}

	@Override
	public Collection<Tenant> getTenants() throws RepositoryException {
		return list();
	}

}

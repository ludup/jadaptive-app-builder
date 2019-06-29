package com.jadaptive.tenant;

import java.util.Collection;

import com.jadaptive.repository.RepositoryException;

public interface TenantService {

	Tenant getCurrentTenant() throws RepositoryException;

	void setCurrentTenant(Tenant tenant);

	void clearCurrentTenant();

	Collection<Tenant> getTenants();

	Tenant createTenant(String name, String hostname) throws RepositoryException;

}

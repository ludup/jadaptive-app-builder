package com.jadaptive.tenant;

import java.util.Collection;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;

public interface TenantService {

	Tenant getCurrentTenant() throws RepositoryException, EntityException;

	void setCurrentTenant(Tenant tenant);

	void clearCurrentTenant();

	Collection<Tenant> getTenants();

	Tenant createTenant(String name, String hostname) throws RepositoryException, EntityException;

	Tenant getSystemTenant() throws RepositoryException, EntityException;

}

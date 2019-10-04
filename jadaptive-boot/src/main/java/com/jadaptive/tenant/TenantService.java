package com.jadaptive.tenant;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;

public interface TenantService {

	public static final String SYSTEM_UUID = "4f1b781c-581d-474f-9505-4fea9c5e3909";
	
	Tenant getCurrentTenant() throws RepositoryException, EntityException;

	void setCurrentTenant(Tenant tenant);

	void clearCurrentTenant();

	Collection<Tenant> getTenants();

	Tenant createTenant(String name, String hostname) throws RepositoryException, EntityException;

	Tenant getSystemTenant() throws RepositoryException, EntityException;

	void setCurrentTenant(HttpServletRequest request);

	Tenant getTenantByName(String tenantName);

	void setCurrentTenant(String name);

}

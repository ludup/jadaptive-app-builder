package com.jadaptive.api.tenant;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.repository.RepositoryException;

public interface TenantService {

	public static final String SYSTEM_UUID = "4f1b781c-581d-474f-9505-4fea9c5e3909";
	
	Tenant getCurrentTenant() throws RepositoryException, EntityException;

	void setCurrentTenant(Tenant tenant);

	void clearCurrentTenant();

	Collection<Tenant> listTenants();

	Tenant getSystemTenant() throws RepositoryException, EntityException;

	void setCurrentTenant(HttpServletRequest request);

	void setCurrentTenant(String name);

	void deleteTenant(Tenant tenant);

	Tenant getTenantByDomainOrDefault(String name);

	Tenant getTenantByDomain(String name);

	void assertManageTenant() throws AccessDeniedException;

	Tenant resolveTenantName(String username);

	Tenant createTenant(String name, String domain, String... additionalDomains)
			throws RepositoryException, EntityException;

	Tenant createTenant(String uuid, String name, String primaryDomain, String... additionalDomains)
			throws RepositoryException, EntityException;

	Tenant createTenant(String uuid, String name, String primaryDomain, boolean system, String... additionalDomains)
			throws RepositoryException, EntityException;

	Tenant getTenantByName(String name);

	Tenant getTenantByUUID(String uuid);

}

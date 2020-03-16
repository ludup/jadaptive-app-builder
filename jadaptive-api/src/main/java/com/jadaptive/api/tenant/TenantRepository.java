package com.jadaptive.api.tenant;

import java.util.Collection;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.RepositoryException;

public interface TenantRepository {

	void saveTenant(Tenant tenant) throws RepositoryException, EntityException;

	void deleteTenant(Tenant tenant) throws RepositoryException, EntityException;
	
	Collection<Tenant> listTenants() throws RepositoryException, EntityException;

	void newSchema() throws RepositoryException, EntityException;

	void dropSchema() throws RepositoryException, EntityException;

	Tenant getTenant(String uuid) throws RepositoryException, EntityException;

	Tenant getSystemTenant() throws RepositoryException, EntityException;

	boolean isEmpty();

	Long countTenants();

	Tenant getTenantByName(String name);

	


}

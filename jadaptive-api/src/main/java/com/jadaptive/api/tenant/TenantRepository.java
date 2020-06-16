package com.jadaptive.api.tenant;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.RepositoryException;

public interface TenantRepository {

	void saveTenant(Tenant tenant) throws RepositoryException, ObjectException;

	void deleteTenant(Tenant tenant) throws RepositoryException, ObjectException;
	
	Iterable<Tenant> listTenants() throws RepositoryException, ObjectException;

	void newSchema() throws RepositoryException, ObjectException;

	void dropSchema() throws RepositoryException, ObjectException;

	Tenant getTenant(String uuid) throws RepositoryException, ObjectException;

	Tenant getSystemTenant() throws RepositoryException, ObjectException;

	boolean isEmpty();

	Long countTenants();

	Tenant getTenantByName(String name);

	


}

package com.jadaptive.tenant;

import java.util.Collection;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;

public interface TenantRepository {

	public static final String SYSTEM_UUID = "4f1b781c-581d-474f-9505-4fea9c5e3909";
	
	void saveTenant(Tenant tenant) throws RepositoryException, EntityException;

	void deleteTenant(Tenant tenant) throws RepositoryException, EntityException;
	
	Collection<Tenant> listTenants() throws RepositoryException, EntityException;

	void newSchema() throws RepositoryException, EntityException;

	void dropSchema() throws RepositoryException, EntityException;

	Tenant getTenant(String uuid) throws RepositoryException, EntityException;

	Tenant getSystemTenant() throws RepositoryException, EntityException;

	boolean isEmpty();

	Long countTenants();

	


}

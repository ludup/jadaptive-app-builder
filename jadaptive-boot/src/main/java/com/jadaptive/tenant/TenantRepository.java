package com.jadaptive.tenant;

import java.util.Collection;

import com.jadaptive.repository.AbstractUUIDRepository;
import com.jadaptive.repository.RepositoryException;

public interface TenantRepository extends AbstractUUIDRepository<Tenant> {

	Collection<Tenant> getTenants() throws RepositoryException;

}

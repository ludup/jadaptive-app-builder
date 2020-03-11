package com.jadaptive.app.tenant;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantRepository;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.AbstractObjectDatabaseImpl;
import com.jadaptive.app.db.DocumentDatabase;

@Repository
public class TenantRepositoryImpl extends AbstractObjectDatabaseImpl implements TenantRepository {

	private static Logger log = LoggerFactory.getLogger(TenantRepositoryImpl.class);
	private static final String TENANT_DATABASE = "tenants";
		
	public TenantRepositoryImpl(DocumentDatabase db) {
		super(db);
	}
	
	@Override
	public void saveTenant(Tenant tenant) throws RepositoryException, EntityException {
		saveObject(tenant, TENANT_DATABASE);
	}
		
	@Override
	public Collection<Tenant> listTenants() throws RepositoryException, EntityException {
		return listObjects(TENANT_DATABASE, Tenant.class);
	}
	
	@Override
	public void deleteTenant(Tenant tenant) throws RepositoryException, EntityException {
		deleteObject(tenant, TENANT_DATABASE);
	}
	
	@Override
	public Tenant getTenant(String uuid) throws RepositoryException, EntityException {
		return getObject(uuid, TENANT_DATABASE, Tenant.class);
	}
	
	@Override
	public Tenant getSystemTenant() throws RepositoryException, EntityException {
		return getTenant(TenantService.SYSTEM_UUID);
	}
	
	@Override
	public void dropSchema() throws RepositoryException, EntityException {
		
		for(Tenant tenant : listTenants()) {
			db.dropDatabase(tenant.getUuid());
		}
		
		db.dropDatabase(TENANT_DATABASE);
		
	}
	
	@Override
	public void newSchema() throws RepositoryException, EntityException {
		
		dropSchema();
		
		if(log.isInfoEnabled()) {
			log.info("Creating new application schema");
		}
	}

	@Override
	public boolean isEmpty() {
		return countObjects(TENANT_DATABASE, Tenant.class) == 0;
	}

	@Override
	public Long countTenants() {
		return countObjects(TENANT_DATABASE, Tenant.class) ;
	}
	
	
		
	

	
}

package com.jadaptive.tenant;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.db.AbstractObjectDatabaseImpl;
import com.jadaptive.db.MongoDatabaseService;
import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.mongodb.client.MongoDatabase;

@Repository
public class TenantRepositoryImpl extends AbstractObjectDatabaseImpl implements TenantRepository {

	private static Logger log = LoggerFactory.getLogger(MongoDatabase.class);
	private static final String TENANT_DATABASE = "tenants";
	
	public static final String SYSTEM_UUID = "4f1b781c-581d-474f-9505-4fea9c5e3909";
	
	@Autowired
	MongoDatabaseService mongo;
	
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
		return getTenant(SYSTEM_UUID);
	}
	
	@Override
	public void dropSchema() throws RepositoryException, EntityException {
		
		for(Tenant tenant : listTenants()) {
			mongo.getClient().dropDatabase(tenant.getUuid());
		}
		
		mongo.getClient().dropDatabase(TENANT_DATABASE);
		
	}
	
	@Override
	public void newSchema() throws RepositoryException, EntityException {
		
		dropSchema();
		
		if(log.isInfoEnabled()) {
			log.info("Creating new application schema");
		}
		
		saveTenant(new Tenant(SYSTEM_UUID, "System", "localhost", true));
	}

	@Override
	public boolean isEmpty() {
		return countObjects(TENANT_DATABASE, Tenant.class) == 0;
	}
	
	
		
	

	
}

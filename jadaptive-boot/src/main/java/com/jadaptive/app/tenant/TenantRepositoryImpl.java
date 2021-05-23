package com.jadaptive.app.tenant;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.cache.CacheService;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantRepository;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.DocumentDatabase;

@Repository
public class TenantRepositoryImpl extends AbstractSystemObjectDatabaseImpl<Tenant> implements TenantRepository {

	private static Logger log = LoggerFactory.getLogger(TenantRepositoryImpl.class);
	private static final String TENANT_DATABASE = "tenants";
		
	@Autowired
	private CacheService cacheService;
	
	public TenantRepositoryImpl(DocumentDatabase db) {
		super(db);
	}
	
	@Override
	public void saveTenant(Tenant tenant) throws RepositoryException, ObjectException {
		saveObject(tenant, TENANT_DATABASE);
	}
		
	@Override
	public Iterable<Tenant> listTenants() throws RepositoryException, ObjectException {
		return listObjects(TENANT_DATABASE, Tenant.class);
	}
	
	@Override
	public void deleteTenant(Tenant tenant) throws RepositoryException, ObjectException {
		deleteObject(tenant, TENANT_DATABASE);
	}
	
	@Override
	public Tenant getTenant(String uuid) throws RepositoryException, ObjectException {
		return getObject(uuid, TENANT_DATABASE, Tenant.class);
	}
	
	@Override
	public Tenant getSystemTenant() throws RepositoryException, ObjectException {
		return getTenant(TenantService.SYSTEM_UUID);
	}
	
	@Override
	public void dropSchema() throws RepositoryException, ObjectException {
		db.dropSchema();
	}
	
	@Override
	public void newSchema() throws RepositoryException, ObjectException {
		
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

	@Override
	public Tenant getTenantByName(String name) {
		return getObject(TENANT_DATABASE, Tenant.class, SearchField.eq("name", name));
	}
	
	@Override
	protected <T extends UUIDEntity> Map<String, T> getCache(Class<T> obj) {
		return cacheService.getCacheOrCreate("tenants.uuidCache", String.class, obj);
	}

	@Override
	public Class<Tenant> getResourceClass() {
		return Tenant.class;
	}	

}

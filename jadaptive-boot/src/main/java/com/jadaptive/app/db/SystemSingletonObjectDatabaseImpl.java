package com.jadaptive.app.db;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.SystemSingletonObjectDatabase;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.SingletonUUIDEntity;
import com.jadaptive.api.tenant.TenantService;

@Repository
public class SystemSingletonObjectDatabaseImpl<T extends SingletonUUIDEntity> implements SystemSingletonObjectDatabase<T> {

	@Autowired
	private TenantAwareObjectDatabase<T> objectDatabase;
	
	@Autowired
	private TenantService tenantService; 
	
	@Override
	public T getObject(Class<T> resourceClass) {
		
		tenantService.setCurrentTenant(tenantService.getSystemTenant());
		try {
			T tmp = resourceClass.getConstructor().newInstance();
			try {
				return objectDatabase.get(tmp.getResourceKey(), resourceClass);
			} catch(ObjectNotFoundException e) {
				Document doc = new Document();
				DocumentHelper.buildDocument(tmp.getResourceKey(), tmp, doc);
				return DocumentHelper.convertDocumentToObject(resourceClass, doc);
			}	
		} catch(Throwable t) {
			throw new RepositoryException(t.getMessage(), t);
		} finally {
			tenantService.clearCurrentTenant();
		}
	}

	@Override
	public void saveObject(T obj) {
		objectDatabase.saveOrUpdate(obj);
	}

}

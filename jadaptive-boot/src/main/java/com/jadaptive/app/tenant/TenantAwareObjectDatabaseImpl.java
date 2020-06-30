package com.jadaptive.app.tenant;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.EventType;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.AbstractObjectDatabaseImpl;
import com.jadaptive.app.db.DocumentDatabase;

@Repository
public class TenantAwareObjectDatabaseImpl<T extends UUIDEntity> 
		extends AbstractObjectDatabaseImpl implements TenantAwareObjectDatabase<T> {

	protected TenantAwareObjectDatabaseImpl(DocumentDatabase db) {
		super(db);
	}
	
	@Autowired
	protected TenantService tenantService;
	
	@Autowired
	protected EventService eventService; 
	
//	@Autowired
//	private CacheService cacheService; 
	
	@Override
	public Iterable<T> list(Class<T> resourceClass, SearchField... fields) {
		return listObjects(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@Override
	public T get(String uuid, Class<T> resourceClass) throws RepositoryException, ObjectException {
		try {
			T result = getObject(uuid, tenantService.getCurrentTenant().getUuid(), resourceClass);
			eventService.publishStandardEvent(EventType.READ, result);
			return result;
		} catch(RepositoryException | ObjectException e) {
			/**
			 * TODO failed read events
			 */
			throw e;
		}
	}
	
	@Override
	public T get(Class<T> resourceClass, SearchField... fields) throws RepositoryException, ObjectException {
		try {
			T result = getObject(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
			eventService.publishStandardEvent(EventType.READ, result);
			return result;
		} catch(RepositoryException | ObjectException e) {
			/**
			 * TODO failed read events
			 */
			throw e;
		}
		
	}
	
	@Override
	public T max(Class<T> resourceClass, String field) throws RepositoryException, ObjectException {
		return max(tenantService.getCurrentTenant().getUuid(), resourceClass, field);
	}
	
	@Override
	public T min(Class<T> resourceClass, String field) throws RepositoryException, ObjectException {
		return min(tenantService.getCurrentTenant().getUuid(), resourceClass, field);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete(T obj) throws RepositoryException, ObjectException {
		delete(obj.getUuid(), (Class<T>) obj.getClass());
	}
	
	@Override
	public void delete(String uuid, Class<T> resourceClass) throws RepositoryException, ObjectException {
		deleteObject(get(uuid, resourceClass), tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public void saveOrUpdate(T obj) throws RepositoryException, ObjectException {
		saveObject(obj, tenantService.getCurrentTenant().getUuid());
	}

	@Override
	public Collection<T> table(String searchField, String searchValue, String order, int start, int length, Class<T> resourceClass) {
		return tableObjects(tenantService.getCurrentTenant().getUuid(), resourceClass, searchField, searchValue, start, length);
	}

	@Override
	public long count(Class<T> resourceClass, SearchField... fields) {
		return countObjects(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}
	
	@Override
	public Collection<T> searchTable(Class<T> resourceClass, int start, int length, SearchField... fields) {
		return searchTable(tenantService.getCurrentTenant().getUuid(), resourceClass, start, length, fields);
	}
	
	@Override
	public Collection<T> searchObjects(Class<T> resourceClass, SearchField... fields) {
		return searchObjects(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@Override
	public Long searchCount(Class<T> resourceClass, SearchField... fields) {
		return searchCount(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}
}

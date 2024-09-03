package com.jadaptive.app.db;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.templates.TemplateUtils;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;

@Repository
public class TenantAwareObjectDatabaseImpl<T extends UUIDEntity> 
		extends AbstractObjectDatabaseImpl implements TenantAwareObjectDatabase<T> {

	public TenantAwareObjectDatabaseImpl(DocumentDatabase db) {
		super(db);
	}

	@Autowired
	protected TenantService tenantService;
	
	@Autowired
	protected EventService eventService; 
	
	protected Tenant getCurrentTenant() {
		return tenantService.getCurrentTenant();
	}
	
	@Override
	public AbstractObject createObject(String resourceKey) {
		return new MongoEntity(resourceKey);
	}
	
	@Override
	public Class<?> getObjectClass() {
		return MongoEntity.class;
	}
	
	@Override
	public Iterable<T> list(Class<T> resourceClass, SearchField... fields) {
		return listObjects(getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@Override
	public <X extends UUIDDocument> X get(String uuid, Class<X> resourceClass) throws RepositoryException, ObjectException {
		try {
			X result = getObject(uuid, getCurrentTenant().getUuid(), resourceClass, 
					SearchField.eq("resourceKey", TemplateUtils.lookupClassResourceKey(resourceClass)));
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
			T result = getObject(getCurrentTenant().getUuid(), resourceClass, fields);
			return result;
		} catch(RepositoryException | ObjectException e) {
			/**
			 * TODO failed read events
			 */
			throw e;
		}
		
	}
	
	@Override
	public T max(Class<T> resourceClass, String field, SearchField... fields) throws RepositoryException, ObjectException {
		return max(getCurrentTenant().getUuid(), resourceClass, field, fields);
	}
	
	@Override
	public T min(Class<T> resourceClass, String field, SearchField... fields) throws RepositoryException, ObjectException {
		return min(getCurrentTenant().getUuid(), resourceClass, field, fields);
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
	public void delete(Class<T> resourceClass, SearchField... fields) throws RepositoryException, ObjectException {
		delete(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}
	
	@Override
	public void deleteIfExists(Class<T> resourceClass, SearchField... fields) throws RepositoryException, ObjectException {
		deleteIfExists(tenantService.getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@Override
	public void saveOrUpdate(T obj) throws RepositoryException, ObjectException {
		saveObject(obj, getCurrentTenant().getUuid());
	}

	@Override
	public Collection<T> table(String searchField, String searchValue, int start, int length, Class<T> resourceClass, SortOrder order, String sortField) {
		return tableObjects(getCurrentTenant().getUuid(), resourceClass, searchField, searchValue, start, length, order, sortField);
	}

	@Override
	public long count(Class<T> resourceClass, SearchField... fields) {
		return countObjects(getCurrentTenant().getUuid(), resourceClass, fields);
	}
	
	@Override
	public Collection<T> searchTable(Class<T> resourceClass, int start, int length, SortOrder order, String sortField, SearchField... fields) {
		return searchTable(getCurrentTenant().getUuid(), resourceClass, start, length, order, sortField, fields);
	}
	
	@Override
	public Collection<T> searchObjects(Class<T> resourceClass, SearchField... fields) {
		return searchObjects(getCurrentTenant().getUuid(), resourceClass, fields);
	}
	
	@Override
	public Collection<T> searchObjects(Class<T> resourceClass, SortOrder order, String sortField, SearchField... fields) {
		return searchObjects(getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@Override
	public Long searchCount(Class<T> resourceClass, SearchField... fields) {
		return searchCount(getCurrentTenant().getUuid(), resourceClass, fields);
	}

	@Override
	public Long sumLongValues(Class<T> resourceClass, String groupBy, SearchField... fields) {
		return sumLongValues(getCurrentTenant().getUuid(), resourceClass, groupBy, fields);
	}
	
	@Override
	public Double sumDoubleValues(Class<T> resourceClass, String groupBy, SearchField... fields) {
		return sumDoubleValues(getCurrentTenant().getUuid(), resourceClass, groupBy, fields);
	}

	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void stashObject(T obj) {
		stash(obj);
	}
}

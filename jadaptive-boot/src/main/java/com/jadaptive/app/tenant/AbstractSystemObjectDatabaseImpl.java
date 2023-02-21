package com.jadaptive.app.tenant;

import java.util.Collection;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.tenant.AbstractTenantAwareObjectDatabase;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.app.db.AbstractObjectDatabaseImpl;
import com.jadaptive.app.db.DocumentDatabase;

public abstract class AbstractSystemObjectDatabaseImpl<T extends AbstractUUIDEntity> 
		extends AbstractObjectDatabaseImpl implements AbstractTenantAwareObjectDatabase<T> {

	
	public AbstractSystemObjectDatabaseImpl(DocumentDatabase db) {
		super(db);
	}

	@Autowired
	protected TenantService tenantService;
	
	@Override
	public Iterable<T> list() throws RepositoryException, ObjectException {
		return listObjects(TenantService.SYSTEM_UUID, getResourceClass());
	}
	
	@Override
	public Iterable<T> list(SearchField... fields) {
		return listObjects(TenantService.SYSTEM_UUID, getResourceClass(), fields);
	}
	
	@Override
	public Collection<T> searchObjects(SearchField... fields) {
		return searchObjects(TenantService.SYSTEM_UUID, getResourceClass(), fields);
	}
	
	@Override
	public Collection<T> searchTable(int start, int length, SortOrder order, String sortField, SearchField... fields) {
		return searchTable(TenantService.SYSTEM_UUID, getResourceClass(), start, length, order, sortField, fields);
	}
	
	@Override
	public Long searchCount(SearchField... fields) {
		return searchCount(TenantService.SYSTEM_UUID, getResourceClass(), fields);
	}

	@Override
	public T get(String uuid) throws RepositoryException, ObjectException {
		return getObject(uuid, TenantService.SYSTEM_UUID, getResourceClass());
	}
	
	protected String getCollectionName(Class<?> clz) {
		ObjectDefinition template = clz.getAnnotation(ObjectDefinition.class);
		while(template!=null && template.type() == ObjectType.OBJECT) {
			clz = clz.getSuperclass();
			template = clz.getAnnotation(ObjectDefinition.class);
		} 
		if(Objects.nonNull(template)) {
			return template.resourceKey();
		}
		throw new ObjectException(String.format("Missing template for class %s", clz.getSimpleName()));
	}
	
	public T get(SearchField... fields) throws RepositoryException, ObjectException {
		return getObject(TenantService.SYSTEM_UUID, getResourceClass(), fields);
	}

	@Override
	public void delete(T obj) throws RepositoryException, ObjectException {
		deleteObject(obj, TenantService.SYSTEM_UUID);
	}

	@Override
	public String saveOrUpdate(T obj) throws RepositoryException, ObjectException {
		saveObject(obj, TenantService.SYSTEM_UUID);
		return obj.getUuid();
	}

	@Override
	public Collection<T> table(String searchField, String searchValue, int start, int length, SortOrder order, String sortField) {
		return tableObjects(TenantService.SYSTEM_UUID, getResourceClass(), searchField, searchValue, start, length, order, sortField);
	}

	@Override
	public long count() {
		return countObjects(TenantService.SYSTEM_UUID, getResourceClass());
	}
	
	@Override
	public void deleteAll() {
		throw new UnsupportedOperationException();
	}

}

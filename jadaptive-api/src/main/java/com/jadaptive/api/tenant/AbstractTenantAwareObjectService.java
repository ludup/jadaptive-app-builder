package com.jadaptive.api.tenant;

import java.util.Collection;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.app.repository.AbstractUUIDEntity;
import com.jadaptive.app.repository.RepositoryException;

public interface AbstractTenantAwareObjectService<T extends AbstractUUIDEntity> {

	T get(String uuid)
			throws RepositoryException, EntityException;

	Collection<T> list() throws RepositoryException, EntityException;
	
	Collection<T> table(String searchField, String searchValue, String order, int start, int length) throws RepositoryException, EntityException;

	void saveOrUpdate(T template) throws RepositoryException, EntityException;

	void delete(String uuid) throws EntityException;
	
	void delete(T obj) throws EntityException;

	long count();

	T get(String field, String value) throws RepositoryException, EntityException;
	
	AbstractTenantAwareObjectDatabase<T> getRepository();
}

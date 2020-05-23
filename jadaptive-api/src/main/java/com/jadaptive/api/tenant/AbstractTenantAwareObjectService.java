package com.jadaptive.api.tenant;

import java.util.Collection;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;

public interface AbstractTenantAwareObjectService<T extends AbstractUUIDEntity> {

	T get(String uuid)
			throws RepositoryException, ObjectException;

	Collection<T> list() throws RepositoryException, ObjectException;
	
	Collection<T> table(String searchField, String searchValue, String order, int start, int length) throws RepositoryException, ObjectException;

	void saveOrUpdate(T template) throws RepositoryException, ObjectException;

	void delete(String uuid) throws ObjectException;
	
	void delete(T obj) throws ObjectException;

	long count();
	
	T get(SearchField... fields) throws RepositoryException, ObjectException;
	
	AbstractTenantAwareObjectDatabase<T> getRepository();

	
}

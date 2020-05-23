package com.jadaptive.api.tenant;

import java.util.Collection;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;

public interface AbstractTenantAwareObjectDatabase<T extends AbstractUUIDEntity> {

	Collection<T> list() throws RepositoryException, ObjectException;

	T get(String uuid) throws RepositoryException, ObjectException;
	
	T get(SearchField... fields) throws RepositoryException, ObjectException;

	void delete(String uuid) throws RepositoryException, ObjectException;

	void saveOrUpdate(T obj) throws RepositoryException, ObjectException;

	Collection<T> table(String searchField, String searchValue, String order, int start, int length);

	long count();

	void delete(T obj) throws RepositoryException, ObjectException;

	Collection<T> list(SearchField... fields);
	
	Class<T> getResourceClass();

	Collection<T> searchObjects(SearchField... fields);

	Collection<T> searchTable(int start, int length, SearchField... fields);

	Long searchCount(SearchField... fields);

}

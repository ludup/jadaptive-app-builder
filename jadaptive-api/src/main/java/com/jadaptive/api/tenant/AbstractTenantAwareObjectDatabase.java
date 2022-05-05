package com.jadaptive.api.tenant;

import java.util.Collection;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.SortOrder;

public interface AbstractTenantAwareObjectDatabase<T extends UUIDDocument> {

	

	T get(String uuid) throws RepositoryException, ObjectException;
	
	T get(SearchField... fields) throws RepositoryException, ObjectException;

	void delete(String uuid) throws RepositoryException, ObjectException;

	String saveOrUpdate(T obj) throws RepositoryException, ObjectException;

	long count();

	void delete(T obj) throws RepositoryException, ObjectException;

	Iterable<T> list() throws RepositoryException, ObjectException;
	
	Iterable<T> list(SearchField... fields);
	
	Class<T> getResourceClass();

	Collection<T> searchObjects(SearchField... fields);

	Long searchCount(SearchField... fields);

	Collection<T> searchTable(int start, int length, SortOrder order, String sortField, SearchField... fields);

	Collection<T> table(String searchField, String searchValue, int start, int length, SortOrder order,
			String sortField);

}

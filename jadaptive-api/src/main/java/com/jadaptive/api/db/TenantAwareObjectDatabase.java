package com.jadaptive.api.db;

import java.util.Collection;
import java.util.Optional;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.SortOrder;

public interface TenantAwareObjectDatabase<T extends UUIDDocument> {

	Iterable<T> list(Class<T> resourceClass, SearchField...fields);
	
	T get(String uuid, Class<T> resourceClass) throws RepositoryException, ObjectException;
	
	T get(Class<T> resourceClass, SearchField... fields) throws RepositoryException, ObjectException;
	
	default Optional<T> getOr(Class<T> resourceClass, SearchField... fields) {
		try {
			return Optional.of(get(resourceClass, fields));
		}
		catch(ObjectNotFoundException onfe) {
			return Optional.empty();
		}
	}

	void delete(T obj) throws RepositoryException, ObjectException;

	void delete(String uuid, Class<T> resourceClass) throws RepositoryException, ObjectException;
	
	void saveOrUpdate(T obj) throws RepositoryException, ObjectException;

	long count(Class<T> resourceClass, SearchField... fields);

	Collection<T> searchObjects(Class<T> resourceClass, SortOrder order, String sortField, SearchField... fields);
	
	Collection<T> searchObjects(Class<T> resourceClass, SearchField... fields);

	Long searchCount(Class<T> resourceClass, SearchField... fields);

	T max(Class<T> resourceClass, String field, SearchField... fields) throws RepositoryException, ObjectException;

	T min(Class<T> resourceClass, String field, SearchField... fields) throws RepositoryException, ObjectException;

	Collection<T> searchTable(Class<T> resourceClass, int start, int length, SortOrder order, String sortField,
			SearchField... fields);

	Collection<T> table(String searchField, String searchValue, int start, int length, Class<T> resourceClass,
			SortOrder order, String sortField);

	Long sumLongValues(Class<T> resourceClass, String groupBy, SearchField... fields);

	void deleteAll();

	void stashObject(T obj);

	void delete(Class<T> resourceClass, SearchField... fields) throws RepositoryException, ObjectException;

	void deleteIfExists(Class<T> resourceClass, SearchField... fields) throws RepositoryException, ObjectException;

	Double sumDoubleValues(Class<T> resourceClass, String groupBy, SearchField... fields);

}

package com.jadaptive.repository;

import java.util.Collection;
import java.util.List;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.tenant.Tenant;

public interface AbstractUUIDRepository<E> {

	void save(List<E> objects, @SuppressWarnings("unchecked") TransactionAdapter<E>... operations) throws RepositoryException;

	void save(E object, @SuppressWarnings("unchecked") TransactionAdapter<E>... operations) throws RepositoryException;

//	List<E> list(String resourceKey) throws RepositoryException;

	E get(String uuid) throws RepositoryException, EntityNotFoundException;

	Collection<E> list(Tenant tenant) throws RepositoryException;
	
//	List<E> list(String resourceKey, QueryParameters parameters) throws RepositoryException;

//	E get(String resourceKey, String column, String value) throws RepositoryException;

}

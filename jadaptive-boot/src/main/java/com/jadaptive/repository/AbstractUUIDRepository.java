package com.jadaptive.repository;

import java.util.Collection;
import java.util.List;

import com.jadaptive.entity.EntityNotFoundException;

public interface AbstractUUIDRepository<E extends AbstractUUIDEntity> {

	String getName();
	
	E createEntity();
	
	void save(List<E> objects, @SuppressWarnings("unchecked") TransactionAdapter<E>... operations) throws RepositoryException;

	void save(E object, @SuppressWarnings("unchecked") TransactionAdapter<E>... operations) throws RepositoryException;
	
	E get(String uuid) throws RepositoryException, EntityNotFoundException;

	Collection<E> list() throws RepositoryException;

	void saveObject(AbstractUUIDEntity e) throws RepositoryException;

}

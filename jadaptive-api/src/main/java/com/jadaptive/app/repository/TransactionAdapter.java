package com.jadaptive.repository;

import com.jadaptive.entity.EntityException;

public interface TransactionAdapter<E> {

	public void afterSave(E object) throws RepositoryException, EntityException;

}

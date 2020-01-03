package com.jadaptive.app.repository;

import com.jadaptive.api.entity.EntityException;

public interface TransactionAdapter<E> {

	public void afterSave(E object) throws RepositoryException, EntityException;

}

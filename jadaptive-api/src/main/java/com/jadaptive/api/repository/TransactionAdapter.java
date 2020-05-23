package com.jadaptive.api.repository;

import com.jadaptive.api.entity.ObjectException;

public interface TransactionAdapter<E> {

	public void afterSave(E object) throws RepositoryException, ObjectException;

}

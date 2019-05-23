package com.jadaptive.templates;

import com.jadaptive.repository.RepositoryException;

public interface TransactionAdapter<E> {

	public void afterSave(E object) throws RepositoryException;

}

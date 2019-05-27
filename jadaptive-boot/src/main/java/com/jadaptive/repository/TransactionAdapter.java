package com.jadaptive.repository;

public interface TransactionAdapter<E> {

	public void afterSave(E object) throws RepositoryException;

}

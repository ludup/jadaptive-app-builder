package com.jadaptive.templates;

import java.util.List;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.repository.TransactionAdapter;

public interface TemplateEnabledUUIDRepository<T extends AbstractUUIDEntity> {

	Integer getWeight();

	T createEntity();

	String getName();

	String getResourceKey();

	Class<T> getResourceClass();

	void saveTemplateObjects(List<T> objects, @SuppressWarnings("unchecked") TransactionAdapter<T>... ops) throws RepositoryException, EntityException;

}

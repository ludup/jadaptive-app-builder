package com.jadaptive.api.templates;

import java.util.List;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.app.repository.AbstractUUIDEntity;
import com.jadaptive.app.repository.RepositoryException;
import com.jadaptive.app.repository.TransactionAdapter;

public interface TemplateEnabledService<T extends AbstractUUIDEntity> {

	Integer getWeight();

	T createEntity();

	String getName();

	String getResourceKey();

	Class<T> getResourceClass();

	void saveTemplateObjects(List<T> objects, @SuppressWarnings("unchecked") TransactionAdapter<T>... ops) throws RepositoryException, EntityException;

	void onTemplatesComplete(String... resourceKeys);

	boolean isSystemOnly();

	String getTemplateFolder();
	

}
package com.jadaptive.api.templates;

import java.util.List;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.TransactionAdapter;

public interface JsonTemplateEnabledService<T extends AbstractUUIDEntity> {

	Integer getTemplateOrder();

	String getName();

	String getResourceKey();

	Class<T> getResourceClass();

	void saveTemplateObjects(List<T> objects, @SuppressWarnings("unchecked") TransactionAdapter<T>... ops) throws RepositoryException, ObjectException;

	boolean isSystemOnly();

	String getTemplateFolder();
	

}

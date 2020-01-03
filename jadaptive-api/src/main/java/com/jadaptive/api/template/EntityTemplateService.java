package com.jadaptive.api.template;

import java.util.Collection;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.app.repository.RepositoryException;

public interface EntityTemplateService {

	EntityTemplate get(String uuid)
			throws RepositoryException, EntityException;

	Collection<EntityTemplate> list() throws RepositoryException, EntityException;
	
	Collection<EntityTemplate> table(String searchField, String searchValue, String order, int start, int length) throws RepositoryException, EntityException;

	void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException;

	void delete(String uuid) throws EntityException;

	long count();

}

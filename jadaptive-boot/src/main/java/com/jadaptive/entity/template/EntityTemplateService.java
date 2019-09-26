package com.jadaptive.entity.template;

import java.util.Collection;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;

public interface EntityTemplateService {

	EntityTemplate get(String uuid)
			throws RepositoryException, EntityException;

	Collection<EntityTemplate> list() throws RepositoryException, EntityException;
	
	Collection<EntityTemplate> table(String search, String order, int start, int length) throws RepositoryException, EntityException;

	void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException;

	void delete(String uuid) throws EntityException;

	long count();

}

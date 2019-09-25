package com.jadaptive.entity.template;

import java.util.Collection;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;

public interface EntityTemplateRepository {

	Collection<EntityTemplate> list() throws RepositoryException, EntityException;

	EntityTemplate get(String resourceKey) throws RepositoryException, EntityException;

	void delete(String uuid) throws RepositoryException, EntityException;

	void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException;

	Collection<EntityTemplate> table(int start, int length);

	long count();

}

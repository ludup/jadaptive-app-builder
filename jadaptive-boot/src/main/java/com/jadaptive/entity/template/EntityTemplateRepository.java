package com.jadaptive.entity.template;

import java.util.Collection;

import com.jadaptive.entity.EntityException;
import com.jadaptive.repository.RepositoryException;
import com.jadaptive.templates.TemplateEnabledUUIDRepository;

public interface EntityTemplateRepository extends TemplateEnabledUUIDRepository<EntityTemplate> {

	Collection<EntityTemplate> list() throws RepositoryException, EntityException;

	EntityTemplate get(String resourceKey) throws RepositoryException, EntityException;

	void delete(String uuid) throws RepositoryException, EntityException;

	void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException;

}

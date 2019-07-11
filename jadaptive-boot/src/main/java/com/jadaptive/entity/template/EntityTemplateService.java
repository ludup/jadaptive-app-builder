package com.jadaptive.entity.template;

import java.util.Collection;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.RepositoryException;

public interface EntityTemplateService {

	EntityTemplate get(String uuid)
			throws RepositoryException, EntityNotFoundException;

	Collection<EntityTemplate> list();

	void saveOrUpdate(EntityTemplate template);

	void delete(String uuid) throws EntityNotFoundException;

}

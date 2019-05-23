package com.jadaptive.entity;

import com.jadaptive.entity.repository.EntityTemplate;
import com.jadaptive.repository.RepositoryException;

public interface EntityTemplateService {

	EntityTemplate get(String resourceKey)
			throws UnknownEntityException, RepositoryException, EntityNotFoundException;

}

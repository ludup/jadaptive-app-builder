package com.jadaptive.entity.template;

import com.jadaptive.entity.EntityNotFoundException;
import com.jadaptive.repository.RepositoryException;

public interface EntityTemplateService {

	EntityTemplate get(String resourceKey)
			throws RepositoryException, EntityNotFoundException;

}

package com.jadaptive.entity;

import com.jadaptive.repository.RepositoryException;

public interface EntityService {

	Entity get(String resourceKey, String uuid) throws RepositoryException, EntityNotFoundException;

}

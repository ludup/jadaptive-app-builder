package com.jadaptive.api.template;

import java.text.ParseException;
import java.util.Collection;
import java.util.Map;

import com.jadaptive.api.entity.EntityException;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDEntity;

public interface EntityTemplateService {

	EntityTemplate get(String uuid)
			throws RepositoryException, EntityException;

	Collection<EntityTemplate> list() throws RepositoryException, EntityException;
	
	Collection<EntityTemplate> table(String searchField, String searchValue, String order, int start, int length) throws RepositoryException, EntityException;

	void saveOrUpdate(EntityTemplate template) throws RepositoryException, EntityException;

	void delete(String uuid) throws EntityException;

	long count();

	<T extends UUIDEntity> T createObject(Map<String, Object> values, Class<T> baseClass) throws ParseException;


}

package com.jadaptive.api.entity;

import java.util.Collection;

import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.template.ValidationException;
import com.jadaptive.api.user.User;

public interface ObjectRepository {

	Iterable<AbstractObject> list(ObjectTemplate def, SearchField... fields) throws RepositoryException, ObjectException;

	AbstractObject getById(ObjectTemplate def, String uuid) throws RepositoryException, ObjectException, ValidationException;

	void deleteByUUID(ObjectTemplate def, String uuid) throws RepositoryException, ObjectException;

	void deleteAll(ObjectTemplate def) throws RepositoryException, ObjectException;

	void deleteByUUIDOrAltId(ObjectTemplate def, String value) throws RepositoryException, ObjectException, ValidationException;

	String save(AbstractObject entity) throws RepositoryException, ObjectException;

	Collection<AbstractObject> table(ObjectTemplate def, int offset, int limit, SearchField... fields);

	long count(ObjectTemplate def, String searchField, String searchValue);

	Collection<AbstractObject> personal(ObjectTemplate def, User currentUser);

	long count(ObjectTemplate def, SearchField... fields);

	
}

package com.jadaptive.api.db;

import java.util.Collection;

import com.jadaptive.api.repository.PersonalUUIDEntity;
import com.jadaptive.api.user.User;

public interface PersonalObjectDatabase<T extends PersonalUUIDEntity> {

	Collection<T> getPersonalObjects(Class<T> resourceClass, User user);

	void saveOrUpdate(T obj, User user);

	Collection<T> getPersonalObjects(Class<T> resourceClass, User user, SearchField... search);


}

package com.jadaptive.api.db;

import java.util.Collection;

import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.user.User;

public interface AssignableObjectDatabase<T extends AssignableUUIDEntity> {

	Collection<T> getAssignedObjects(Class<T> resourceClass, User user);

}

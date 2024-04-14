package com.jadaptive.api.db;

import com.jadaptive.api.entity.AbstractObject;

public interface AbstractObjectDatabase {

	AbstractObject createObject(String resourceKey);

	Class<?> getObjectClass();
}

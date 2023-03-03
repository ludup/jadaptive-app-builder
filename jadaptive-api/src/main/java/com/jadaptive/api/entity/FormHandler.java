package com.jadaptive.api.entity;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.repository.UUIDEntity;

public interface FormHandler  extends ExtensionPoint {

	String getResourceKey();

	<T extends UUIDEntity> String saveObject(T object);
}

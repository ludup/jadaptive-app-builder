package com.jadaptive.api.entity;

import java.util.Collection;
import java.util.Map;

import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.template.FieldTemplate;

public interface AbstractEntity extends UUIDDocument {

	AbstractEntity getChild(FieldTemplate c);

	String getResourceKey();

	void setResourceKey(String resourceKey);

	Object getValue(String fieldName);

	Object getValue(FieldTemplate t);

	void setValue(FieldTemplate t, Object value);

	Boolean isSystem();

	String getUuid();

	Map<String, Object> getDocument();

	Boolean isHidden();

	Collection<AbstractEntity> getObjectCollection(String resourceKey);

	Collection<String> getCollection(String resourceKey);

}

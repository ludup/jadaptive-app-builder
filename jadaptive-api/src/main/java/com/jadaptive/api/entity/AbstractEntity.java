package com.jadaptive.api.entity;

import com.jadaptive.api.template.FieldTemplate;

public interface AbstractEntity {

	AbstractEntity getChild(FieldTemplate c);

	String getResourceKey();

	void setResourceKey(String resourceKey);

	Object getValue(String fieldName);

	Object getValue(FieldTemplate t);

	void setValue(FieldTemplate t, Object value);

}

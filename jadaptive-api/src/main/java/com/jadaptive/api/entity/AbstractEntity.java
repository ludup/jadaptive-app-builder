package com.jadaptive.api.entity;

import com.jadaptive.api.template.FieldTemplate;

public interface AbstractEntity {

	AbstractEntity getChild(FieldTemplate c);

	String getResourceKey();

	void setResourceKey(String resourceKey);

	String getValue(String fieldName);

	String getValue(FieldTemplate t);

	void setValue(FieldTemplate t, String value);

}

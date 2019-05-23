package com.jadaptive.entity.repository;

public interface FieldMetaValue extends AbstractUUIDEntity {

	String getResourceKey();

	void setResourceKey(String resourceKey);

	String getValue();

	void setValue(String value);

	FieldTemplate getTemplate();

	void setTemplate(FieldTemplate template);

	FieldMetaType getType();

}

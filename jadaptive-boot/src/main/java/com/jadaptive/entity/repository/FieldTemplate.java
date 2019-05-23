package com.jadaptive.entity.repository;

import java.util.Collection;

import com.jadaptive.entity.template.FieldMetaValueImpl;

public interface FieldTemplate extends AbstractUUIDEntity {

	String getResourceKey();

	void setResourceKey(String resourceKey);

	String getDefaultValue();

	void setDefaultValue(String defaultValue);

	String getDescription();

	void setDescription(String description);

	FieldType getFieldType();

	void setFieldType(FieldType propertyType);

	Integer getWeight();

	void setWeight(Integer weight);

	Boolean getHidden();

	void setHidden(Boolean hidden);

	void setSearchable(Boolean searchable);

	Boolean getSearchable();

	void setMetaValue(String resourceKey, Integer value);

	void setMetaValue(String resourceKey, Long value);

	void setMetaValue(String resourceKey, Double value);

	void setMetaValue(String resourceKey, Boolean value);

	void setMetaValue(String resourceKey, String value);

	Collection<FieldMetaValueImpl> getMetaValues();

}

package com.jadaptive.entity.template;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.jadaptive.repository.AbstractUUIDEntity;
import com.jadaptive.repository.NamedUUIDEntity;

public class FieldTemplate extends NamedUUIDEntity {
	
	String resourceKey;
	String defaultValue;
	String description;
	FieldType fieldType; 
	Integer weight;
	boolean searchable; 
	boolean required;
	boolean defaultColumn;
	boolean ignoreColumn;
	
	Set<FieldValidator> validators;
	
	public FieldTemplate() {
	}

	public String getResourceKey() {
		return StringUtils.defaultString(resourceKey);
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getDefaultValue() {
		return StringUtils.defaultString(defaultValue);
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		return StringUtils.defaultString(description);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType propertyType) {
		this.fieldType = propertyType;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Boolean getSearchable() {
		return searchable;
	}

	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}
	
	public boolean isDefaultColumn() {
		return defaultColumn;
	}

	public void setDefaultColumn(Boolean defaultColumn) {
		this.defaultColumn = defaultColumn;
	}

	public Set<FieldValidator> getValidators() {
		return validators;
	}

	public void setValidators(Set<FieldValidator> validators) {
		this.validators = validators;
	}

	public int hashCode() {
		return  new HashCodeBuilder(7, 43)
				.append(getUuid())
				.append(resourceKey)
				.append(fieldType.ordinal()).build();
	}

	public boolean equals(Object obj) {
		if(obj instanceof FieldTemplate) {
			FieldTemplate template = (FieldTemplate) obj;
		return new EqualsBuilder().append(getUuid(), template.getUuid())
				.append(resourceKey, template.getResourceKey())
				.append(fieldType.ordinal(), template.getFieldType().ordinal()).build();
		}
		return false;
	}

	public boolean getRequired() {
		return false;
	}

	public boolean isIgnoreColumn() {
		return ignoreColumn;
	}

	public void setIgnoreColumn(Boolean ignoreColumn) {
		this.ignoreColumn = ignoreColumn;
	}

	public String getValidationValue(ValidationType type) {
		for(FieldValidator v : validators) {
			if(type==v.getType()) {
				return v.getValue();
			}
		}
		throw new IllegalStateException(String.format("There is no validator for type %s on field %s", type.name(), getResourceKey()));
	}

	
}

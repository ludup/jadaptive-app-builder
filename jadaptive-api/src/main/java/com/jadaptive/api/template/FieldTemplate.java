package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.jadaptive.api.repository.NamedUUIDEntity;

public class FieldTemplate extends NamedUUIDEntity {
	
	String resourceKey;
	String defaultValue;
	String description;
	FieldType fieldType; 
	boolean required;
	
	Collection<FieldValidator> validators = new ArrayList<>();
	
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

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Collection<FieldValidator> getValidators() {
		return validators;
	}

	public void setValidators(Collection<FieldValidator> validators) {
		this.validators = validators;
	}

	public int hashCode() {
		return  new HashCodeBuilder(7, 43)
				.append(getUuid())
				.append(resourceKey)
				.append(fieldType.ordinal()).toHashCode();
	}

	public boolean equals(Object obj) {
		if(obj instanceof FieldTemplate) {
			FieldTemplate template = (FieldTemplate) obj;
		return new EqualsBuilder().append(getUuid(), template.getUuid())
				.append(resourceKey, template.getResourceKey())
				.append(fieldType.ordinal(), template.getFieldType().ordinal()).isEquals();
		}
		return false;
	}

	public boolean getRequired() {
		return required;
	}

//	public boolean isIgnoreColumn() {
//		return ignoreColumn;
//	}
//
//	public void setIgnoreColumn(Boolean ignoreColumn) {
//		this.ignoreColumn = ignoreColumn;
//	}

	public String getValidationValue(ValidationType type) {
		for(FieldValidator v : validators) {
			if(type==v.getType()) {
				return v.getValue();
			}
		}
		throw new IllegalStateException(String.format("There is no validator for type %s on field %s", type.name(), getResourceKey()));
	}

	
}

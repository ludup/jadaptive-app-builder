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
	boolean collection;
	boolean required;
	boolean encrypted;
	boolean searchable;
	boolean unique;
	boolean textIndex;
	boolean readOnly;
	
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

	public boolean isRequired() {
		return required;
	}

	public boolean getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public boolean getCollection() {
		return collection;
	}

	public void setCollection(boolean collection) {
		this.collection = collection;
	}

	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isTextIndex() {
		return textIndex;
	}

	public void setTextIndex(boolean textIndex) {
		this.textIndex = textIndex;
	}

	public String getValidationValue(ValidationType type) {
		for(FieldValidator v : validators) {
			if(type==v.getType()) {
				return v.getValue();
			}
		}
		throw new IllegalStateException(String.format("There is no validator for type %s on field %s", type.name(), getResourceKey()));
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}

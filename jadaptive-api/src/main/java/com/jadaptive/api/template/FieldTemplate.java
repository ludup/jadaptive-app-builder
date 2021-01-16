package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;

@ObjectDefinition(resourceKey = FieldTemplate.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
public class FieldTemplate extends AbstractUUIDEntity {

	private static final long serialVersionUID = -9164781667373808388L;

	public static final String RESOURCE_KEY = "objectFields";
	
	@ObjectField(type = FieldType.TEXT, required = true)
	String resourceKey;
	
	@ObjectField(type = FieldType.TEXT, required = true)
	String formVariable;
	
	@ObjectField(type = FieldType.TEXT)
	String defaultValue;
	
	@ObjectField(type = FieldType.TEXT)
	String description;
	
	@ObjectField(type = FieldType.ENUM, required = true)
	FieldType fieldType; 
	
	@ObjectField(type = FieldType.BOOL)
	boolean collection;
	
	@ObjectField(type = FieldType.BOOL)
	boolean required;
	
	@ObjectField(type = FieldType.BOOL)
	boolean encrypted;
	
	@ObjectField(type = FieldType.BOOL)
	boolean searchable;
	
	@ObjectField(type = FieldType.BOOL)
	boolean unique;
	
	@ObjectField(type = FieldType.BOOL)
	boolean textIndex;
	
	@ObjectField(type = FieldType.BOOL)
	boolean readOnly;
	
	@ObjectField(type = FieldType.BOOL)
	boolean alternativeId;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = FieldValidator.RESOURCE_KEY)
	Collection<FieldValidator> validators = new ArrayList<>();
	
	@ObjectField(type = FieldType.ENUM)
	Collection<FieldView> views = new ArrayList<>();
	
	@ObjectField(type = FieldType.TEXT)
	Collection<String> viewPermissions = new ArrayList<>();
	
	@ObjectField(type = FieldType.BOOL)
	boolean requireAllPermissions = false;
	
	public FieldTemplate() {
	}

	public String getResourceKey() {
		return StringUtils.defaultString(resourceKey);
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getFormVariable() {
		return formVariable;
	}

	public void setFormVariable(String formVariable) {
		this.formVariable = formVariable;
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

	public void setRequired(boolean required) {
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

	public boolean isAlternativeId() {
		return alternativeId;
	}

	public void setAlternativeId(boolean alternativeId) {
		this.alternativeId = alternativeId;
	}

	public Collection<FieldView> getViews() {
		return views;
	}

	public void setViews(Collection<FieldView> views) {
		this.views = views;
	}

	public Collection<String> getViewPermissions() {
		return viewPermissions;
	}

	public void setViewPermissions(Collection<String> viewPermissions) {
		this.viewPermissions = viewPermissions;
	}

	public boolean isRequireAllPermissions() {
		return requireAllPermissions;
	}

	public void setRequireAllPermissions(boolean requireAllPermissions) {
		this.requireAllPermissions = requireAllPermissions;
	}
}

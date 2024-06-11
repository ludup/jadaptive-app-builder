package com.jadaptive.api.template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;

@ObjectDefinition(resourceKey = FieldTemplate.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
public class FieldTemplate extends TemplateUUIDEntity {

	private static final long serialVersionUID = -9164781667373808388L;

	public static final String RESOURCE_KEY = "objectFields";
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String resourceKey;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String parentKey;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String parentField;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String formVariable;
	
	@ObjectField(type = FieldType.TEXT)
	String defaultValue;
	
	@ObjectField(type = FieldType.ENUM)
	@Validator(type = ValidationType.REQUIRED)
	FieldType fieldType; 
	
	@ObjectField(type = FieldType.BOOL)
	boolean collection;
	
	@ObjectField(type = FieldType.BOOL)
	boolean hidden;
	
	@ObjectField(type = FieldType.BOOL)
	boolean summarise;
	
	@ObjectField(type = FieldType.BOOL)
	boolean manuallyEncrypted;
	
	@ObjectField(type = FieldType.BOOL)
	boolean automaticallyEncrypted;
	
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
	
	@ObjectField(type = FieldType.BOOL)
	boolean cascadeDelete;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED, references = FieldValidator.RESOURCE_KEY)
	Collection<FieldValidator> validators = new ArrayList<>();
	
	@ObjectField(type = FieldType.ENUM)
	Collection<FieldView> views = new ArrayList<>();
	
	@ObjectField(type = FieldType.TEXT)
	Collection<String> viewPermissions = new ArrayList<>();
	
	@ObjectField(type = FieldType.BOOL)
	boolean requireAllPermissions = false;
	
	@ObjectField(type = FieldType.BOOL)
	boolean resettable = false;
	
	@ObjectField(type = FieldType.TEXT_AREA)
	String meta;
	
	public FieldTemplate() {
	}

	public boolean isResettable() {
		return resettable;
	}

	public void setResettable(boolean resettable) {
		this.resettable = resettable;
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

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType propertyType) {
		this.fieldType = propertyType;
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
		for(FieldValidator v : validators) {
			if(v.getType() == ValidationType.REQUIRED) {
				return true;
			}
		}
		return false;
	}

	public boolean isManuallyEncrypted() {
		return manuallyEncrypted;
	}

	public void setManuallyEncrypted(boolean manuallyEncrypted) {
		this.manuallyEncrypted = manuallyEncrypted;
	}

	public boolean isAutomaticallyEncrypted() {
		return automaticallyEncrypted;
	}

	public void setAutomaticallyEncrypted(boolean automaticallyEncrypted) {
		this.automaticallyEncrypted = automaticallyEncrypted;
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
		throw new UnsupportedOperationException(String.format("There is no validator for type %s on field %s", type.name(), getResourceKey()));
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

	public String getParentKey() {
		return parentKey;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}

	public String getParentField() {
		return parentField;
	}

	public void setParentField(String parentField) {
		this.parentField = parentField;
	}

	public boolean isSummarise() {
		return summarise;
	}

	public void setSummarise(boolean summarise) {
		this.summarise = summarise;
	}

	public int getValidationValueInt(ValidationType val, int defaultValue) {
		try {
			return Integer.parseInt(getValidationValue(val));
		} catch(Throwable e) {
			return defaultValue;
		}
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}
	
	public String getMetaValue(String name, String defaultValue) {
		Map<String,String> data = generateMap();
		String value = data.get(name);
		if(Objects.isNull(value)) {
			return defaultValue;
		} else {
			return value;
		}
	}
	
	public int getMetaValueInt(String name, int defaultValue) {
		Map<String,String> data = generateMap();
		String value = data.get(name);
		if(Objects.isNull(value)) {
			return defaultValue;
		} else {
			return Integer.parseInt(value);
		}
	}
	
	private Map<String,String> generateMap() {
		
		String[] values = meta.split(",");
		Map<String,String> data = new HashMap<>();
		for(String value : values) {
			data.put(StringUtils.substringBefore(value, "="),
					StringUtils.substringAfter(value, "="));
		}
		return data;
	}

	public boolean isCascadeDelete() {
		return cascadeDelete;
	}

	public void setCascadeDelete(boolean cascadeDelete) {
		this.cascadeDelete = cascadeDelete;
	}
}

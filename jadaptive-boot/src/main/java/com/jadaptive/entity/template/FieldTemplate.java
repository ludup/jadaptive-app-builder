package com.jadaptive.entity.template;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jadaptive.repository.AbstractUUIDEntity;

@JsonSerialize(using=FieldTemplateSerializer.class)
public class FieldTemplate extends AbstractUUIDEntity {
	
	String resourceKey;
	String defaultValue;
	String description;
	FieldType fieldType; 
	Integer weight;
	boolean hidden;
	boolean searchable; 
	
	Collection<FieldValidator> validators;
	
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
	
	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Boolean getSearchable() {
		return searchable;
	}

	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
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
	
	public void toMap(Map<String,String> properties) {
		
		properties.put("resourceKey", resourceKey);
		properties.put("defaultValue", defaultValue);
		properties.put("description", description);
		properties.put("fieldType", fieldType.name());
		properties.put("weight", String.valueOf(weight));
		properties.put("hidden", String.valueOf(hidden));
		properties.put("searchable", String.valueOf(searchable));

	}
	
	public void fromMap(String uuid, Map<String, String> properties) {
		
		setUuid(uuid);
		
		resourceKey = properties.get("resourceKey");
		defaultValue = properties.get("defaultValue");
		description = properties.get("description");
		fieldType = FieldType.valueOf(properties.get("fieldType"));
		weight = Integer.parseInt(properties.get("weight"));
		hidden = Boolean.valueOf(properties.get("hidden"));
		searchable  = Boolean.valueOf(properties.get("searchable"));
	}
}

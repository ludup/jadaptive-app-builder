package com.jadaptive.entity.template;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jadaptive.entity.repository.AbstractUUIDObject;
import com.jadaptive.entity.repository.FieldMetaType;
import com.jadaptive.entity.repository.FieldMetaValue;
import com.jadaptive.entity.repository.FieldTemplate;
import com.jadaptive.entity.repository.FieldType;

@JsonSerialize(using=FieldTemplateSerializer.class)
public class FieldTemplateImpl extends AbstractUUIDObject implements FieldTemplate {
	
	String resourceKey;
	String defaultValue;
	String description;
	FieldType fieldType; 
	Integer weight;
	boolean hidden;
	boolean searchable; 
	
	Collection<FieldMetaValueImpl> metadata = new HashSet<FieldMetaValueImpl>();
	
	public FieldTemplateImpl() {
	}

	@Override
	public String getResourceKey() {
		return StringUtils.defaultString(resourceKey);
	}

	@Override
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	@Override
	public String getDefaultValue() {
		return StringUtils.defaultString(defaultValue);
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String getDescription() {
		return StringUtils.defaultString(description);
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public FieldType getFieldType() {
		return fieldType;
	}

	@Override
	public void setFieldType(FieldType propertyType) {
		this.fieldType = propertyType;
	}

	@Override
	public Integer getWeight() {
		return weight;
	}

	@Override
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	
	@Override
	public Boolean getHidden() {
		return hidden;
	}

	@Override
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public Boolean getSearchable() {
		return searchable;
	}

	@Override
	public void setSearchable(Boolean searchable) {
		this.searchable = searchable;
	}

	public int hashCode() {
		return  new HashCodeBuilder(7, 43)
				.append(getUuid())
				.append(resourceKey)
				.append(fieldType.ordinal()).build();
	}

	public boolean equals(Object obj) {
		if(obj instanceof FieldTemplateImpl) {
			FieldTemplateImpl template = (FieldTemplateImpl) obj;
		return new EqualsBuilder().append(getUuid(), template.getUuid())
				.append(resourceKey, template.getResourceKey())
				.append(fieldType.ordinal(), template.getFieldType().ordinal()).build();
		}
		return false;
	}
	
	@Override
	public void setMetaValue(String resourceKey, Integer value) {
		setMetaValue(resourceKey, String.valueOf(value), FieldMetaType.NUMBER);
	}
	
	@Override
	public void setMetaValue(String resourceKey, Long value) {
		setMetaValue(resourceKey, String.valueOf(value), FieldMetaType.NUMBER);
	}
	
	@Override
	public void setMetaValue(String resourceKey, Double value) {
		setMetaValue(resourceKey, String.valueOf(value), FieldMetaType.DECIMAL);
	}
	
	@Override
	public void setMetaValue(String resourceKey, Boolean value) {
		setMetaValue(resourceKey, String.valueOf(value), FieldMetaType.BOOLEAN);
	}
	
	@Override
	public void setMetaValue(String resourceKey, String value) {
		setMetaValue(resourceKey, value, FieldMetaType.TEXT);
	}
	
	private void setMetaValue(String resourceKey, String value, FieldMetaType type) {
		for(FieldMetaValue v : metadata) {
			if(v.getResourceKey().equals(resourceKey)) {
				v.setValue(value);
				return;
			}
		}
		metadata.add(new FieldMetaValueImpl(type, this, resourceKey, value));
	}

	@Override
	public Collection<FieldMetaValueImpl> getMetaValues() {
		return metadata;
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

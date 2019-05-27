package com.jadaptive.entity.template;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.entity.FieldMetaType;
import com.jadaptive.repository.AbstractUUIDEntity;

public class FieldMetaValue extends AbstractUUIDEntity {

	String resourceKey;
	String value;
	FieldTemplate template; 
	FieldMetaType type; 
	
	public FieldMetaValue() {
	}
	
	public FieldMetaValue(FieldMetaType type, FieldTemplate template, String resourceKey, String value) {
		this.type = type;
		this.template = template;
		this.resourceKey = resourceKey;
		this.value = value;
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@JsonIgnore
	public FieldTemplate getTemplate() {
		return template;
	}

	public void setTemplate(FieldTemplate template) {
		this.template = template;
	}

	public FieldMetaType getType() {
		return type;
	}
}

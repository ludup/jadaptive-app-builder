package com.jadaptive.entity.template;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jadaptive.entity.repository.AbstractUUIDEntityImpl;
import com.jadaptive.entity.repository.FieldMetaType;
import com.jadaptive.entity.repository.FieldMetaValue;
import com.jadaptive.entity.repository.FieldTemplate;

public class FieldMetaValueImpl extends AbstractUUIDEntityImpl implements FieldMetaValue {

	String resourceKey;
	String value;
	FieldTemplate template; 
	FieldMetaType type; 
	
	public FieldMetaValueImpl() {
	}
	
	public FieldMetaValueImpl(FieldMetaType type, FieldTemplateImpl template, String resourceKey, String value) {
		this.type = type;
		this.template = template;
		this.resourceKey = resourceKey;
		this.value = value;
	}

	@Override
	public String getResourceKey() {
		return resourceKey;
	}

	@Override
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@JsonIgnore
	@Override
	public FieldTemplate getTemplate() {
		return template;
	}

	@Override
	public void setTemplate(FieldTemplate template) {
		this.template = template;
	}

	@Override
	public FieldMetaType getType() {
		return type;
	}
}

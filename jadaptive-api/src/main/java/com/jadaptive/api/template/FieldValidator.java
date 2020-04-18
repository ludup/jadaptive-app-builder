package com.jadaptive.api.template;

import com.jadaptive.api.repository.AbstractUUIDEntity;

public class FieldValidator extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "fieldValidator";
	
	ValidationType type;
	String value;
	
	public FieldValidator() {
	}
	
	public FieldValidator(ValidationType type, String value) {
		this.type = type;
		this.value = value;
	}

	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	public ValidationType getType() {
		return type;
	}

	public void setType(ValidationType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	
}

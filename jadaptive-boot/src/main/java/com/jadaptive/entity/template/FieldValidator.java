package com.jadaptive.entity.template;

import com.jadaptive.entity.ValidationType;

public class FieldValidator {

	ValidationType type;
	String value;
	
	public FieldValidator() {

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

package com.jadaptive.entity.template;

public enum ValidationType {

	RANGE(false), 
	LENGTH(false),
	REGEX(false),
	OBJECT_TYPE(true);
	
	final boolean required;
	
	ValidationType(boolean required) {
		this.required = required;
	}

	public final boolean isRequired() {
		return required;
	}
}

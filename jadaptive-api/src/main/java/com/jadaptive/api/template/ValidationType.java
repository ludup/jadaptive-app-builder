package com.jadaptive.api.template;

public enum ValidationType {

	RANGE(false), 
	LENGTH(false),
	REGEX(false),
	OBJECT_TYPE(true),
	RESOURCE_KEY(true),
	URL(false),
	REQUIRED(false), 
	IMAGE_HEIGHT(false),
	IMAGE_WIDTH(false),
	CLASSES(false);
	
	final boolean required;
	
	ValidationType(boolean required) {
		this.required = required;
	}

	public final boolean isRequired() {
		return required;
	}
}

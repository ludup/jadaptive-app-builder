package com.jadaptive.api.template;

public enum FieldType {

	BOOL,
	LONG(ValidationType.RANGE),
	DECIMAL(ValidationType.RANGE),
	TEXT(ValidationType.LENGTH, ValidationType.REGEX),
	TEXT_AREA(ValidationType.LENGTH, ValidationType.REGEX),
	PASSWORD(ValidationType.LENGTH, ValidationType.REGEX),
	OBJECT_REFERENCE(ValidationType.OBJECT_TYPE, ValidationType.RESOURCE_KEY),
	OBJECT_EMBEDDED(ValidationType.OBJECT_TYPE, ValidationType.RESOURCE_KEY), 
	ENUM(ValidationType.OBJECT_TYPE), 
	TIMESTAMP,
	INTEGER(ValidationType.RANGE),
	DATE,
	PERMISSION, 
	HIDDEN,
	IMAGE;
	
	ValidationType[] options;
	
	FieldType(ValidationType...options) {
		this.options = options;
	}

	public ValidationType[] getOptions() {
		return options;
	}
}

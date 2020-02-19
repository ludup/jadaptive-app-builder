package com.jadaptive.api.template;

public enum FieldType {

	BOOL,
	NUMBER(ValidationType.RANGE),
	DECIMAL(ValidationType.RANGE),
	TEXT(ValidationType.LENGTH, ValidationType.REGEX),
	TEXT_AREA(ValidationType.LENGTH, ValidationType.REGEX),
	PASSWORD(ValidationType.LENGTH, ValidationType.REGEX),
	OBJECT_REFERENCE(ValidationType.OBJECT_TYPE),
	OBJECT_EMBEDDED(ValidationType.OBJECT_TYPE), 
	ENUM(ValidationType.OBJECT_TYPE), 
	DATE;
	
	ValidationType[] options;
	
	FieldType(ValidationType...options) {
		this.options = options;
	}

	public ValidationType[] getOptions() {
		return options;
	}
}

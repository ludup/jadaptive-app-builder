package com.jadaptive.entity.template;

public enum FieldType {

	BOOL,
	NUMBER(ValidationType.RANGE),
	DECIMAL(ValidationType.RANGE),
	TEXT(ValidationType.LENGTH, ValidationType.REGEX),
	TEXT_AREA(ValidationType.LENGTH, ValidationType.REGEX),
	COUNTRY,
	OBJECT_REFERENCE(ValidationType.OBJECT_TYPE),
	OBJECT_EMBEDDED(ValidationType.OBJECT_TYPE);
	
	ValidationType[] options;
	
	FieldType(ValidationType...options) {
		this.options = options;
	}

	public ValidationType[] getOptions() {
		return options;
	}
}

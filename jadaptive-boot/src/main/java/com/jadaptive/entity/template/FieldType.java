package com.jadaptive.entity.template;

public enum FieldType {

	CHECKBOX,
	NUMBER(ValidationType.RANGE),
	DECIMAL(ValidationType.RANGE),
	TEXT(ValidationType.LENGTH, ValidationType.REGEX),
	TEXT_AREA(ValidationType.LENGTH, ValidationType.REGEX),
	COUNTRY,
	OBJECT_REFERENCE(ValidationType.OBJECT),
	OBJECT_COLLECTION(ValidationType.OBJECT);
	
	ValidationType[] options;
	
	FieldType(ValidationType...options) {
		this.options = options;
	}

	public ValidationType[] getOptions() {
		return options;
	}
}

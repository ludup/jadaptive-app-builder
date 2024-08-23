package com.jadaptive.api.template;

public enum FieldType {

	BOOL(true),
	LONG(true, ValidationType.RANGE),
	DECIMAL(true, ValidationType.RANGE),
	TEXT(true, ValidationType.LENGTH, ValidationType.REGEX),
	TEXT_AREA(true, ValidationType.LENGTH, ValidationType.REGEX),
	PASSWORD(true, ValidationType.LENGTH, ValidationType.REGEX),
	OBJECT_REFERENCE(ValidationType.OBJECT_TYPE, ValidationType.RESOURCE_KEY),
	OBJECT_EMBEDDED(ValidationType.OBJECT_TYPE, ValidationType.RESOURCE_KEY), 
	ENUM(true, ValidationType.OBJECT_TYPE), 
	TIMESTAMP,
	INTEGER(true, ValidationType.RANGE),
	DATE,
	PERMISSION(true), 
//	HIDDEN,
	IMAGE,
	FILE,
	COUNTRY, 
	OPTIONS,
	TIME,
	ATTACHMENT, 
	TEMPLATE_REFERENCE;
	
	ValidationType[] options;
	boolean canDefault = false;
	
	FieldType(ValidationType...options) {
		this.options = options;
	}

	FieldType(boolean canDefault, ValidationType...options) {
		this.canDefault = canDefault;
		this.options = options;
	}

	public ValidationType[] getOptions() {
		return options;
	}
	
	public boolean canDefault() {
		return canDefault;
	}
}

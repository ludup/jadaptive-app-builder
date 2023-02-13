package com.jadaptive.api.template;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;

@ObjectDefinition(resourceKey = FieldValidator.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
public class FieldValidator extends TemplateUUIDEntity {

	private static final long serialVersionUID = 5940642806418577079L;

	public static final String RESOURCE_KEY = "fieldValidators";
	
	@ObjectField(type = FieldType.ENUM)
	@Validator(type = ValidationType.REQUIRED)
	ValidationType type;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String value;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String i18n;
	
	@ObjectField(type = FieldType.TEXT)
	@Validator(type = ValidationType.REQUIRED)
	String bundle;
	
	public FieldValidator() {
	}
	
	public FieldValidator(ValidationType type, String value, String bundle) {
		this.type = type;
		this.value = value;
		this.bundle = bundle;
		this.i18n = "";
	}
	
	public FieldValidator(ValidationType type, String value, String bundle, String i18n) {
		this.type = type;
		this.value = value;
		this.bundle = bundle;
		this.i18n = i18n;
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

	public String getI18n() {
		return i18n;
	}

	public void setI18n(String i18n) {
		this.i18n = i18n;
	}

	public String getBundle() {
		return bundle;
	}
	
	public void setBundle(String bundle) {
		this.bundle = bundle;
	}
	
}

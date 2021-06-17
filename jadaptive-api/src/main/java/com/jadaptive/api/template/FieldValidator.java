package com.jadaptive.api.template;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;

@ObjectDefinition(resourceKey = FieldValidator.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
public class FieldValidator extends AbstractUUIDEntity {

	private static final long serialVersionUID = 5940642806418577079L;

	public static final String RESOURCE_KEY = "fieldValidators";
	
	@ObjectField(type = FieldType.ENUM, required = true)
	ValidationType type;
	
	@ObjectField(type = FieldType.TEXT, required = true)
	String value;
	
	@ObjectField(type = FieldType.TEXT, required = true)
	String i18n;
	
	@ObjectField(type = FieldType.TEXT, required = true)
	String bundle;
	
	public FieldValidator() {
	}
	
	public FieldValidator(ValidationType type, String value) {
		this.type = type;
		this.value = value;
		this.bundle = "";
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

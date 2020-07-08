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
	
	public FieldValidator() {
	}
	
	public FieldValidator(ValidationType type, String value) {
		this.type = type;
		this.value = value;
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

	
}

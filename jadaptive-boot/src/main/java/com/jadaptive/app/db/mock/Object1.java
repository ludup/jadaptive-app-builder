package com.jadaptive.app.db.mock;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = Object1.RESOURCE_KEY)
public class Object1 extends AbstractUUIDEntity {

	private static final long serialVersionUID = -1022570617998580427L;

	public static final String RESOURCE_KEY = "object1";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@ObjectField(type = FieldType.TEXT)
	String field1;

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	
}

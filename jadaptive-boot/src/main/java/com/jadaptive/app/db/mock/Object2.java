package com.jadaptive.app.db.mock;

import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;

//@ObjectDefinition(resourceKey = Object2.RESOURCE_KEY)
public class Object2 extends AbstractUUIDEntity {

	private static final long serialVersionUID = -1022570617998580427L;

	public static final String RESOURCE_KEY = "object2";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@ObjectField(type = FieldType.TEXT)
	String field2;

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

	
	
}

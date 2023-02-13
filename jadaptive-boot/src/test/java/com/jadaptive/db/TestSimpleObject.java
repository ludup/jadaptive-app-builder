package com.jadaptive.db;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

@ObjectDefinition(resourceKey = TestSimpleObject.RESOURCE_KEY, type = ObjectType.OBJECT)
public class TestSimpleObject extends TestUUIDEntity {
	
	private static final long serialVersionUID = 1254047875320294802L;
	public static final String RESOURCE_KEY = "testSimpleObject";

	public TestSimpleObject(String name) {
		setName(name);
	}
	
	@ObjectField(searchable = true, unique = true, type = FieldType.TEXT, nameField = true)
	@Validator(type = ValidationType.REQUIRED)
	protected String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}

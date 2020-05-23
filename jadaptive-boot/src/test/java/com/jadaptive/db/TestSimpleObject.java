package com.jadaptive.db;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.Template;

@Template(name = "Test Simple Object", resourceKey = TestSimpleObject.RESOURCE_KEY, type = ObjectType.OBJECT)
public class TestSimpleObject extends NamedUUIDEntity {
	
	public static final String RESOURCE_KEY = "testSimpleObject";

	public TestSimpleObject() {

	}
	
	public TestSimpleObject(String name) {
		setName(name);
	}	
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}

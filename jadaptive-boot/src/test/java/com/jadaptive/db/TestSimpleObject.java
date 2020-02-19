package com.jadaptive.db;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.Template;

@Template(name = "Test Simple Object", resourceKey = "testSimpleObject", type = EntityType.OBJECT)
public class TestSimpleObject extends NamedUUIDEntity {
	
	public TestSimpleObject() {

	}
	
	public TestSimpleObject(String name) {
		setName(name);
	}	

}

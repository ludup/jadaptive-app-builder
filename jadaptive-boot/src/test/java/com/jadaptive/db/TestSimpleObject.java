package com.jadaptive.db;

import com.jadaptive.api.repository.NamedUUIDEntity;

public class TestSimpleObject extends NamedUUIDEntity {
	
	public TestSimpleObject() {

	}
	
	public TestSimpleObject(String name) {
		setName(name);
	}	

}

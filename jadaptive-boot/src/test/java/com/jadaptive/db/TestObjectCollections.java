package com.jadaptive.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.jadaptive.api.repository.AbstractUUIDEntity;

public class TestObjectCollections extends AbstractUUIDEntity {

	List<TestSimpleObject> values;
	
	public TestObjectCollections() {
		
	}
	
	public TestObjectCollections(TestSimpleObject... elements) {
		this.values = Arrays.asList(elements);
	}

	public List<TestSimpleObject> getValues() {
		return values;
	}

	public void setValues(Collection<TestSimpleObject> values) {
		this.values = new ArrayList<>(values);
	}


}

package com.jadaptive.db;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.api.repository.AbstractUUIDEntity;

public class TestEnumCollections extends AbstractUUIDEntity {

	Collection<TestEnum> values;
	
	public TestEnumCollections() {
		
	}
	
	public TestEnumCollections(TestEnum... elements) {
		this.values = Arrays.asList(elements);
	}

	public Collection<TestEnum> getValues() {
		return values;
	}

	public void setValues(Collection<TestEnum> values) {
		this.values = values;
	}


}

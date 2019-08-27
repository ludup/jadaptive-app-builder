package com.jadaptive.db;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.repository.AbstractUUIDEntity;

public class TestStringCollections extends AbstractUUIDEntity {

	Collection<String> strings;
	
	public TestStringCollections() {
		
	}
	
	public TestStringCollections(String... elements) {
		this.strings = Arrays.asList(elements);
	}

	public Collection<String> getStrings() {
		return strings;
	}

	public void setStrings(Collection<String> strings) {
		this.strings = strings;
	}

}

package com.jadaptive.db;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(name = "Test String Collections", resourceKey = TestStringCollections.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class TestStringCollections extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "testStringCollections";
	
	@ObjectField(name = "Strings", description = "A collectionn of strings", type = FieldType.TEXT)
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

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}

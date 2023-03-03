package com.jadaptive.db;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = TestStringCollections.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class TestStringCollections extends TestUUIDEntity {

	private static final long serialVersionUID = -222634087659887670L;

	public static final String RESOURCE_KEY = "testStringCollections";
	
	@ObjectField(type = FieldType.TEXT)
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

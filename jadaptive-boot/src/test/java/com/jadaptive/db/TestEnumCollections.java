package com.jadaptive.db;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = TestEnumCollections.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class TestEnumCollections extends TestUUIDEntity {

	private static final long serialVersionUID = 2379492937315526686L;

	public static final String RESOURCE_KEY = "testEnumCollections";
	
	@ObjectField(type = FieldType.ENUM)
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

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}


}

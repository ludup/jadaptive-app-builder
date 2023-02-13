package com.jadaptive.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = TestObjectCollections.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class TestObjectCollections extends TestUUIDEntity {

	private static final long serialVersionUID = 6201675368007087525L;

	public static final String RESOURCE_KEY = "testObjectCollections";
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
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

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

}

package com.jadaptive.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = TestObjectCollections.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class TestObjectCollections extends AbstractUUIDEntity {

	private static final long serialVersionUID = 6201675368007087525L;

	public static final String RESOURCE_KEY = "testObjectCollections";
	
	@ObjectField(name = "Values", description = "A collection of objects", type = FieldType.OBJECT_EMBEDDED)
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

package com.jadaptive.db;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Test Enum Collections", resourceKey = TestEnumCollections.RESOURCE_KEY, type = ObjectType.COLLECTION)
public class TestEnumCollections extends AbstractUUIDEntity {

	public static final String RESOURCE_KEY = "testEnumCollections";
	
	@Column(name = "Values", description = "A collection of enums", type = FieldType.ENUM)
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

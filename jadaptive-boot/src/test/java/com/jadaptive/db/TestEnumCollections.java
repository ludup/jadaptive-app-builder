package com.jadaptive.db;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Test Enum Collections", resourceKey = "testEnumCollections", type = EntityType.COLLECTION)
public class TestEnumCollections extends AbstractUUIDEntity {

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


}

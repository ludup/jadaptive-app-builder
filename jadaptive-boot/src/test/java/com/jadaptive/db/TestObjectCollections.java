package com.jadaptive.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Test Object Collections", resourceKey = "testObjectCollections", type = EntityType.COLLECTION)
public class TestObjectCollections extends AbstractUUIDEntity {

	@Column(name = "Values", description = "A collection of objects", type = FieldType.OBJECT_EMBEDDED)
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

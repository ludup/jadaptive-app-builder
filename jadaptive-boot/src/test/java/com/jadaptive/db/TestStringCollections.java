package com.jadaptive.db;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.api.entity.EntityType;
import com.jadaptive.api.repository.AbstractUUIDEntity;
import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.Template;

@Template(name = "Test String Collections", resourceKey = "testStringCollections", type = EntityType.COLLECTION)
public class TestStringCollections extends AbstractUUIDEntity {

	@Column(name = "Strings", description = "A collectionn of strings", type = FieldType.TEXT)
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

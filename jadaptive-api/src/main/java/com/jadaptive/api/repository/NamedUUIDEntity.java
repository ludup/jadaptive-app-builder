package com.jadaptive.api.repository;

import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;

public abstract class NamedUUIDEntity extends AbstractUUIDEntity {

	private static final long serialVersionUID = 2690511827179922811L;

	@ObjectField(searchable = true, unique = true, type = FieldType.TEXT)
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

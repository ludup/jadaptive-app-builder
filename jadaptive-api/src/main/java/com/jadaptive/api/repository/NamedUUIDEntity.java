package com.jadaptive.api.repository;

import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;

public abstract class NamedUUIDEntity extends AbstractUUIDEntity {

	private static final long serialVersionUID = 2690511827179922811L;

	@ObjectField(searchable = true, type = FieldType.TEXT, nameField = true)
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

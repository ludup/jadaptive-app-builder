package com.jadaptive.api.repository;

import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

public abstract class ReadOnlyNamedUUIDEntity extends AbstractUUIDEntity implements NamedDocument {

	private static final long serialVersionUID = 2690511827179922811L;

	@ObjectField(searchable = true, unique = true, type = FieldType.TEXT, nameField = true, readOnly = true)
	@Validator(type = ValidationType.REQUIRED)
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

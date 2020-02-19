package com.jadaptive.api.repository;

import com.jadaptive.api.template.Column;
import com.jadaptive.api.template.FieldType;

public class NamedUUIDEntity extends AbstractUUIDEntity {

	@Column(name = "Name", description = "The name of this object", type = FieldType.TEXT)
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

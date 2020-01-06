package com.jadaptive.api.repository;

public class NamedUUIDEntity extends AbstractUUIDEntity {

	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

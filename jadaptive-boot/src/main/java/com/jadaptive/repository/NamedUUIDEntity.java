package com.jadaptive.repository;

import java.text.ParseException;
import java.util.Map;

public class NamedUUIDEntity extends AbstractUUIDEntity {

	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void toMap(Map<String,String> properties) throws ParseException {
		super.toMap(properties);
		properties.put("name", getName());
	}
	
	public void fromMap(Map<String,String> properties) throws ParseException {
		super.fromMap(properties);
		this.name = properties.get("name");
	}
}

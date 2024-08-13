package com.jadaptive.api.repository;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = UUIDReference.RESOURCE_KEY, type = ObjectType.OBJECT)
public class UUIDReference extends UUIDEntity implements NamedDocument {

	private static final long serialVersionUID = 8704359424299916453L;

	public static final String RESOURCE_KEY = "uuidReference";

	@ObjectField(type = FieldType.TEXT)
	String name;

	public UUIDReference() { }
	
	public UUIDReference(String uuid, String name) {
		setUuid(uuid);
		this.name = name;
	}
	
	public UUIDReference(NamedDocument e) {
		this(e.getUuid(), e.getName());
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
}

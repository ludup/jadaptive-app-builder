package com.jadaptive.api.repository;

public class UUIDReference extends UUIDEntity implements NamedDocument {

	private static final long serialVersionUID = 8704359424299916453L;

	public static final String RESOURCE_KEY = "uuidReference";

	String name;

	public UUIDReference() { }
	
	public UUIDReference(String uuid, String name) {
		setUuid(uuid);
		this.name = name;
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

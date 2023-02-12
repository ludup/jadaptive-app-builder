package com.jadaptive.api.repository;

public abstract class UUIDEntity implements UUIDDocument {

	private static final long serialVersionUID = 4463484601829889960L;
	
	String uuid;
	
	public abstract String getResourceKey();
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getEventGroup() {
		return getResourceKey();
	}
}

package com.jadaptive.api.repository;

public abstract class UUIDEntity implements UUIDDocument {

	private static final long serialVersionUID = 4463484601829889960L;
	
	String uuid;
	Boolean system;
	Boolean hidden;
	
	public abstract String getResourceKey();
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public Boolean isSystem() {
		return system==null ? Boolean.FALSE : system;
	}

	public void setSystem(Boolean system) {
		this.system = system;
	}

	public Boolean isHidden() {
		return hidden==null ? Boolean.FALSE : hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	
	
}

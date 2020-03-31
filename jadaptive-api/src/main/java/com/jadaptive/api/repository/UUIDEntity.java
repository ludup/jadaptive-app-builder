package com.jadaptive.api.repository;

public abstract class UUIDEntity {

	String uuid;
	Boolean system;
	Boolean hidden;
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public Boolean getSystem() {
		return system==null ? Boolean.FALSE : system;
	}

	public void setSystem(Boolean system) {
		this.system = system;
	}

	public Boolean getHidden() {
		return hidden==null ? Boolean.FALSE : hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
}

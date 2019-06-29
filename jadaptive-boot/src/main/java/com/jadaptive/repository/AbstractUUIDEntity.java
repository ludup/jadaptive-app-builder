package com.jadaptive.repository;

import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractUUIDEntity {

	String uuid;
	Boolean system;
	Boolean hidden;
	
	public String getUuid() {
		return Objects.isNull(uuid) ? UUID.randomUUID().toString() : uuid;
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

	public void toMap(Map<String,String> properties) throws ParseException {
		
		properties.put("uuid", getUuid());
		properties.put("system", String.valueOf(system));
		properties.put("hidden", String.valueOf(system));
	}
	
	public void fromMap(Map<String,String> properties) throws ParseException {
		this.uuid = properties.get("uuid");
		this.system = Boolean.valueOf(properties.get("system"));
		this.hidden = Boolean.valueOf(properties.get("hidden"));
	}
	
	
}

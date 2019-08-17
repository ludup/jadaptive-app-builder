package com.jadaptive.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractUUIDEntity {

	static Logger log = LoggerFactory.getLogger(AbstractUUIDEntity.class);
			
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

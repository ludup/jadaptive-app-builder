package com.jadaptive.tenant;

import com.jadaptive.repository.AbstractUUIDEntity;

public class Tenant extends AbstractUUIDEntity {

	String name;
	String hostname;
	
	public Tenant() {
	}

	public Tenant(String uuid, String name, String hostname) {
		this.setUuid(uuid);
		this.name = name;
		this.hostname = hostname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}

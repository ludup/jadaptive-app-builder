package com.jadaptive.tenant;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.jadaptive.datasource.DataSourceEntity;
import com.jadaptive.repository.AbstractUUIDEntity;

public class Tenant extends AbstractUUIDEntity implements DataSourceEntity {

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

	@Override
	public void store(Map<String, Map<String, String>> properties) throws ParseException {
		properties.put(getUuid(), new HashMap<>());
		super.toMap(properties.get(getUuid()));
		properties.get(getUuid()).put("name", name);
		properties.get(getUuid()).put("hostname", hostname);
	}

	@Override
	public void load(String uuid, Map<String, Map<String, String>> properties) throws ParseException {
		super.fromMap(properties.get(uuid));
		this.name = properties.get(uuid).get("name");
		this.hostname = properties.get(uuid).get("hostname");
	}
}

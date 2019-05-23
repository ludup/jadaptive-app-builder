package com.jadaptive.entity.repository;

import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractUUIDEntityImpl implements AbstractUUIDEntity {

	String uuid;
	
	@Override
	public String getUuid() {
		return Objects.isNull(uuid) ? UUID.randomUUID().toString() : uuid;
	}

	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void toMap(Map<String,String> properties) throws ParseException {
		properties.put("uuid", getUuid());
	}
	
	public void fromMap(Map<String,String> properties) throws ParseException {
		this.uuid = properties.get("uuid");
	}
}

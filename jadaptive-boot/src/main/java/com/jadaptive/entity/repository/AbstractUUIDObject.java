package com.jadaptive.entity.repository;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractUUIDObject {

	String uuid;

	public String getUuid() {
		if(Objects.isNull(uuid)) {
			uuid = UUID.randomUUID().toString();
		}
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}

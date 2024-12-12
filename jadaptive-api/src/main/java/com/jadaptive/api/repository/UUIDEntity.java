package com.jadaptive.api.repository;

import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UUIDEntity other = (UUIDEntity) obj;
		return Objects.equals(uuid, other.uuid);
	}
}

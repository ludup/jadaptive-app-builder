package com.jadaptive.api.repository;

public abstract class PersonalUUIDEntity extends AbstractUUIDEntity {

	String ownerUUID;

	public String getOwnerUUID() {
		return ownerUUID;
	}

	public void setOwnerUUID(String ownerUUID) {
		this.ownerUUID = ownerUUID;
	}
	
	
}

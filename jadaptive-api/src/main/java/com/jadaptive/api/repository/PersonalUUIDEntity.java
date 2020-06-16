package com.jadaptive.api.repository;

public abstract class PersonalUUIDEntity extends AbstractUUIDEntity {

	private static final long serialVersionUID = -8870385785883925751L;
	
	String ownerUUID;

	public String getOwnerUUID() {
		return ownerUUID;
	}

	public void setOwnerUUID(String ownerUUID) {
		this.ownerUUID = ownerUUID;
	}
	
	
}

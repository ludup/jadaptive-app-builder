package com.jadaptive.api.repository;

public abstract class SingletonUUIDEntity extends AbstractUUIDEntity {

	private static final long serialVersionUID = -3854788593572978599L;

	@Override
	public String getUuid() {
		return getResourceKey();
	}

	@Override
	public void setUuid(String uuid) {
		
	}

	
}

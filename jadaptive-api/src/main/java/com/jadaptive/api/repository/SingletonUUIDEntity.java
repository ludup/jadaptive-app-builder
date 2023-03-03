package com.jadaptive.api.repository;

public abstract class SingletonUUIDEntity extends AbstractUUIDEntity {

	private static final long serialVersionUID = -3854788593572978599L;

	@Override
	public final String getUuid() {
		return getResourceKey();
	}

	@Override
	public final void setUuid(String uuid) {
		
	}

	
}

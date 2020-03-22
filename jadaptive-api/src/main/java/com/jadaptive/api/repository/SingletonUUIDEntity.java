package com.jadaptive.api.repository;

public abstract class SingletonUUIDEntity extends AbstractUUIDEntity {

	@Override
	public String getUuid() {
		return getSingletonUuid();
	}

	protected abstract String getSingletonUuid();
	
	@Override
	public void setUuid(String uuid) {
		throw new UnsupportedOperationException("Singleton UUID cannot be set");
	}

	
}

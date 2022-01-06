package com.jadaptive.api.events;

import com.jadaptive.api.repository.UUIDEntity;

public class UUIDEntityEvent<T extends UUIDEntity> extends SystemEvent {

	private static final long serialVersionUID = 6015292582999672923L;

	T object;
	public UUIDEntityEvent(String resourceKey, String group, T object) {
		super(resourceKey, group);
		this.object = object;
	}
	
	public T getObject() {
		return object;
	}
	
	@Override
	public boolean async() { return false; };

}

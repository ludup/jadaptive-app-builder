package com.jadaptive.api.events;

import com.jadaptive.api.repository.UUIDEntity;

public class UUIDEntityDeletedEvent<T extends UUIDEntity> extends ObjectEvent<T> {

	private static final long serialVersionUID = 6015292582999672923L;

	T object;
	
	public UUIDEntityDeletedEvent(T object) {
		super(Events.deleted(object.getEventGroup()), object.getEventGroup());
		this.object = object;
	}

	@Override
	public T getObject() {
		return object;
	}
}

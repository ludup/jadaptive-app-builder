package com.jadaptive.api.events;

import com.jadaptive.api.repository.UUIDEntity;

public class UUIDEntityUpdatedEvent<T extends UUIDEntity> extends UUIDEntityEvent<T> {

	private static final long serialVersionUID = 6015292582999672923L;

	T previousObject;
	
	public UUIDEntityUpdatedEvent(T object, T previousObject) {
		super(Events.updated(object.getEventGroup()), object.getEventGroup(), object);
		this.previousObject = previousObject;
	}
	
	public T getPreviousObject() {
		return previousObject;
	}

}

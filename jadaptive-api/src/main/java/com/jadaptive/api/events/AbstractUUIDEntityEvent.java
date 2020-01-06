package com.jadaptive.api.events;

import com.jadaptive.api.repository.AbstractUUIDEntity;

public class AbstractUUIDEntityEvent<T extends AbstractUUIDEntity> extends SystemEvent {

	private static final long serialVersionUID = 1L;

	T obj;
	
	public AbstractUUIDEntityEvent(Object source, DefaultEventType type, T obj) {
		super(source, String.format("%s.%s", obj.getClass().getName(), type.name()));
		this.obj = obj;
	}
	
	public AbstractUUIDEntityEvent(Object source, DefaultEventType type, T obj, Throwable e) {
		super(source, String.format("%s.%s", obj.getClass().getName(), type.name()), e);
		this.obj = obj;
	}

	public T getObject() {
		return obj;
	}
}

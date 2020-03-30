package com.jadaptive.app.events;

import org.springframework.context.ApplicationEvent;

public class SystemEvent<T> extends ApplicationEvent {

	private static final long serialVersionUID = 4068966863055480029L;

	T object;
	
	public SystemEvent(T source) {
		super(source);
	}

	public T getObject() {
		return object;
	}
}

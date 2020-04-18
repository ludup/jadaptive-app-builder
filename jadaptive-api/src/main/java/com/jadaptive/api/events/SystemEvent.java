package com.jadaptive.api.events;

import org.springframework.context.ApplicationEvent;

public class SystemEvent<T> extends ApplicationEvent {

	private static final long serialVersionUID = 4068966863055480029L;

	T object;
	String resourceKey;
	
	public SystemEvent(T source, String resourceKey) {
		super(source);
		this.resourceKey = resourceKey;
	}

	public T getObject() {
		return object;
	}

	public String getResourceKey() {
		return resourceKey;
	}
}

package com.jadaptive.api.events;

import org.springframework.context.ApplicationEvent;

public class SystemEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	String eventName;
	boolean success;
	Throwable e;
	
	public SystemEvent(Object source, String eventName) {
		super(source);
		this.eventName = eventName;
		this.success = true;
	}
	
	public SystemEvent(Object source, String eventName, Throwable e) {
		super(source);
		this.eventName = eventName;
		this.success = false;
		this.e = e;
	}

	public String getEventName() {
		return eventName;
	}

	public boolean isSuccess() {
		return success;
	}
	
	public Throwable getError() {
		return e;
	}
}

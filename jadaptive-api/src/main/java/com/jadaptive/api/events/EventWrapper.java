package com.jadaptive.api.events;

import org.springframework.context.ApplicationEvent;

public class EventWrapper extends ApplicationEvent {

	private static final long serialVersionUID = -214286129911533919L;

	AuditEvent event;
	
	public EventWrapper(Object source, AuditEvent event) {
		super(source);
		this.event = event;
	}

	public AuditEvent getEvent() {
		return event;
	}
}

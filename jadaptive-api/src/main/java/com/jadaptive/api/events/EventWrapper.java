package com.jadaptive.api.events;

import org.springframework.context.ApplicationEvent;

public class EventWrapper extends ApplicationEvent {

	private static final long serialVersionUID = -214286129911533919L;

	CustomEvent event;
	
	public EventWrapper(Object source, CustomEvent event) {
		super(source);
		this.event = event;
	}

	public CustomEvent getEvent() {
		return event;
	}
}

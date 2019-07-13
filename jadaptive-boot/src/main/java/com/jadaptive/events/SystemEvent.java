package com.jadaptive.events;

import org.springframework.context.ApplicationEvent;

public class SystemEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	public SystemEvent(Object source) {
		super(source);
	}

}

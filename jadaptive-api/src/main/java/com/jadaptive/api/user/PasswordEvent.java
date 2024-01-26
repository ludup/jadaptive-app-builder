package com.jadaptive.api.user;

import com.jadaptive.api.events.UserGeneratedEvent;

public abstract class PasswordEvent extends UserGeneratedEvent {

	private static final long serialVersionUID = -3313630376543682370L;
	
	@Deprecated
	public PasswordEvent() { 
		
	}

	public PasswordEvent(String resourceKey) {
		super(resourceKey, "users");
	}
	
	public PasswordEvent(String resourceKey, Throwable t) {
		super(resourceKey, "users", t);
	}
}

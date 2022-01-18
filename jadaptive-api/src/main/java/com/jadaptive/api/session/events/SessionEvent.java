package com.jadaptive.api.session.events;

import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.session.Session;


public class SessionEvent extends ObjectEvent<Session> {

	private static final long serialVersionUID = -6350681450369361249L;

	Session session;
	
	public SessionEvent(String resourceKey, Session session) {
		super(resourceKey, "sessions");
		this.session = session;
	}

	@Override
	public Session getObject() {
		return session;
	}

}

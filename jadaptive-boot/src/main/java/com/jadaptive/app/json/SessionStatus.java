package com.jadaptive.app.json;

import com.jadaptive.api.session.Session;

public class SessionStatus extends RequestStatus {

	Session session;
	String homePage;
	
	public SessionStatus() {
		super();
	}

	public SessionStatus(String message) {
		super(false, message);
	}

	public SessionStatus(Session session, String homePage) {
		super(true, "");
		this.session = session;
		this.homePage = homePage;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getHomePage() {
		return homePage;
	}

	public void setHomePage(String homePage) {
		this.homePage = homePage;
	}
}

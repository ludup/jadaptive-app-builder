package com.jadaptive.api.json;

public class RedirectStatus extends RequestStatusImpl {
	
	public RedirectStatus(String location) {
		super(true, location);
	}

	public boolean isRedirect() {
		return true;
	}
	
	public String getLocation() {
		return getMessage();
	}
}

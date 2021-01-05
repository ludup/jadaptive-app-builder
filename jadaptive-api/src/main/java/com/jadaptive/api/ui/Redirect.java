package com.jadaptive.api.ui;

public abstract class Redirect extends RuntimeException {

	private static final long serialVersionUID = -385462794687076495L;

	
	public abstract String getUri();
}

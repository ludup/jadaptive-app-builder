package com.jadaptive.api.ui;

public class UriRedirect extends Redirect {

	private static final long serialVersionUID = -8712697700670101012L;
	
	String uri;
	public UriRedirect(String uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}

}

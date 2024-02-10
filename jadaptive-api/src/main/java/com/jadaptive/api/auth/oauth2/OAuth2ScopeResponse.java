package com.jadaptive.api.auth.oauth2;

public class OAuth2ScopeResponse<BODY> extends OAuth2ErrorResponse {

	private BODY body;

	public OAuth2ScopeResponse() {
		this(null);
	}

	public OAuth2ScopeResponse(BODY body) {
		super();
		this.body = body;
	}

	public OAuth2ScopeResponse(String error, Exception exception) {
		super(error, exception);
	}

	public OAuth2ScopeResponse(String error, String errorDescription) {
		super(error, errorDescription);
	}

	public BODY getBody() {
		return body;
	}

	public void setBody(BODY body) {
		this.body = body;
	}
}

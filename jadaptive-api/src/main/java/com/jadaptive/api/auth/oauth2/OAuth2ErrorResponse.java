package com.jadaptive.api.auth.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuth2ErrorResponse implements OAuth2Response {
	private String error;
	private String errorDescription;
	
	protected OAuth2ErrorResponse() {
		this(null, (String)null);
	}

	public OAuth2ErrorResponse(String error, Exception exception) {
		this(error, exception.getMessage() == null ? null : exception.getMessage());
	}
	
	public OAuth2ErrorResponse(String error) {
		this(error, (String)null);
	}

	public OAuth2ErrorResponse(String error, String errorDescription) {
		super();
		this.error = error;
		this.errorDescription = errorDescription;
	}

	@JsonProperty("error")
	public String getError() {
		return error;
	}

	@JsonProperty("error_description")
	public String getErrorDescription() {
		return errorDescription;
	}

}
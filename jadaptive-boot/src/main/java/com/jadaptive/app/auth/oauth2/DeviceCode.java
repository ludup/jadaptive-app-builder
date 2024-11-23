package com.jadaptive.app.auth.oauth2;

import com.jadaptive.api.auth.oauth2.OAuth2Response;

public final class DeviceCode implements OAuth2Response {

	public String device_code;
	public long expires_in;
	public String user_code;
	public String verification_uri;
	public String verification_uri_complete;
	public int interval;
	
	DeviceCode(String device_code, long expires_in, String user_code, String verification_uri, int interval, String verification_uri_complete) {
		super();
		this.device_code = device_code;
		this.expires_in = expires_in;
		this.user_code = user_code;
		this.verification_uri = verification_uri;
		this.interval = interval;
		this.verification_uri_complete = verification_uri_complete;
	}
}
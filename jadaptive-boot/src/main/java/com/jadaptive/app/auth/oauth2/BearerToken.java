package com.jadaptive.app.auth.oauth2;

import com.jadaptive.api.auth.oauth2.OAuth2Response;
import com.jadaptive.api.auth.oauth2.OAuth2Token;

public final class BearerToken implements OAuth2Response {
	public String access_token;
	public long expires_in;
	public String nonce;
	public String token_type = "Bearer";
	public String refresh_token;
	
	public BearerToken(OAuth2Token token, long expiresIn) {
		access_token = token.getName();
		refresh_token = token.getRefreshToken();
		expires_in = expiresIn;
		nonce = token.getNonce();
	}
	
}
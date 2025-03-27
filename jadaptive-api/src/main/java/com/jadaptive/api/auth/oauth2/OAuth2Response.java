package com.jadaptive.api.auth.oauth2;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

public interface OAuth2Response {
	
	public static void authenticateResponse(HttpServletResponse response, String realm) throws IOException {
		response.addHeader("WWW-Authenticate", "Bearer realm=\"" + realm + "\"");
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}
}
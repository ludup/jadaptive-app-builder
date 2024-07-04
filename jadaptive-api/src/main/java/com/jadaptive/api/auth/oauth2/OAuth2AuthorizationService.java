package com.jadaptive.api.auth.oauth2;

import java.util.UUID;

import org.springframework.web.util.UrlPathHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuth2AuthorizationService {

	public static record OAuth2Token(String token, String refreshToken, String tokenType, String state, long expires) { }
	
	public interface OAuth2Authorized {
		void handleAuthorization(OAuth2Token token, HttpServletRequest request, HttpServletResponse response,
				OAuth2Authorization authorization) throws Exception;
	}

	public static class OAuth2Authorization {
		public static final String ATTRIBUTE_NAME = OAuth2Authorization.class.getName();

		private final String state;
		private String browserUri;
		private final String tokenUri;
		private final String codeVerifier;
		private final String redirectUri;
		private final String clientId;
		private final OAuth2Authorized onAuthorized;

		public OAuth2Authorization(String browserUri, 
				OAuth2Request req, OAuth2Authorized onAuthorized) {
			this.clientId = req.clientId();
			this.browserUri = browserUri;
			this.codeVerifier = req.codeVerifier();
			this.redirectUri = req.redirectUri();
			this.state = req.state();
			this.onAuthorized = onAuthorized;

			tokenUri = req.baseUri() + "/oauth2/token"; 
			
		}

		public String getRedirectUri() {
			return redirectUri;
		}

		public String getCodeVerifier() {
			return codeVerifier;
		}

		public String getTokenUri() {
			return tokenUri;
		}

		public String getBrowserUri() {
			return browserUri;
		}

		public String getState() {
			return state;
		}

		protected void setBrowserUri(String browserUri) {
			this.browserUri = browserUri;
		}

		protected final void handleAuthorization(OAuth2Token token, HttpServletRequest request,
				HttpServletResponse response,
				OAuth2Authorization authorization) throws Exception {
			onAuthorized.handleAuthorization(token, request, response, authorization);
		}

		public String getClientId() {
			return clientId;
		}

	}

	void expectAuthorize(OAuth2Authorization auth);

	public static String genToken() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	OAuth2Authorization get(String state);

	static String resolveUri(HttpServletRequest req, String path) {
		
		var b = new StringBuffer();
		b.append(req.getScheme());
		b.append("://");
		b.append(req.getServerName());
		if((req.getServerPort() != 443 && req.getScheme().equals("https")) ||
		   (req.getServerPort() != 80 && req.getScheme().equals("http"))) {
			b.append(':');
			b.append(req.getServerPort());
		}
		if(path.startsWith("/")) {
			b.append(path);
		}
		else {
			return new UrlPathHelper().getPathWithinApplication(req) + path;
		}
		return b.toString();
	}
}

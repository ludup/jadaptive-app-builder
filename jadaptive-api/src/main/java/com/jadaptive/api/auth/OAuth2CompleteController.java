package com.jadaptive.api.auth;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class OAuth2CompleteController {
	public static final String PATH_PREFIX = "/oauth2/end";
	
	static final Logger LOG = LoggerFactory.getLogger(OAuth2CompleteController.class);

	public static class OAuth2Token {
		private String token;
		private String refreshToken;
		private long expires;

		public OAuth2Token(String token, String refreshToken, long expires) {
			super();
			this.token = token;
			this.refreshToken = refreshToken;
			this.expires = expires;
		}

		public String getToken() {
			return token;
		}

		public String getRefreshToken() {
			return refreshToken;
		}

		public long getExpires() {
			return expires;
		}

	}
	
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

			tokenUri = req.baseUri() + "/app/api/oauth2/token"; 
			
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

	private Map<String, OAuth2Authorization> authorizations = new HashMap<>();

	@RequestMapping(value = OAuth2CompleteController.PATH_PREFIX, method = RequestMethod.GET, produces = "text/plain")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public void completeOAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
		synchronized (authorizations) {
			var state = request.getParameter("state");
			if (state == null) {
				LOG.error("No state parameter provided for oauth2 handler.");
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			var c = authorizations.get(state);
			if (c == null) {
				LOG.warn("LogonState has expired.");
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			request.setAttribute(OAuth2Authorization.ATTRIBUTE_NAME, c);

			/*
			 * If the authhorized service returned an error, then redirect to the original
			 * URI and show an error message.
			 */
			var error = request.getParameter("error");
			var errorDescription = request.getParameter("errorDescription");
			if (isNotBlank(error)) {
				if (isBlank(errorDescription)) {
					errorDescription = "The authorization server returned the error '" + error + "'";
				}
			}
			
			var redirectTo = c.getBrowserUri();

			try {
				if (isNotBlank(errorDescription)) {
					throw new IllegalStateException(
							"Failure OAuth response. " + errorDescription);
				}

				/* Get expected parameters */
				var code = request.getParameter("code");
				if (isBlank(code)) {
					throw new IllegalArgumentException("No code parameter provided for oauth2 handler.");
				}

				LOG.info("Handling oauth reply for state {}. Token URI is {}", state, c.getTokenUri());

				/*
				 * Exchange the authorization code for an access token, but instead of providing
				 * a pre-registered client secret, you send the PKCE secret generated at the
				 * beginning of the flow.
				 */
				var parameters = new HashMap<String, String>();
				parameters.put("grant_type", "authorization_code");
				parameters.put("code", code);
				parameters.put("redirect_uri", c.getRedirectUri());
				parameters.put("client_id", c.getClientId());
				parameters.put("code_verifier", c.getCodeVerifier());

//				var headers = new HashMap<String, String>();

				/*
				 * TODO this is really really bad and relies on a really really bad hack in the
				 * hypersocket server.
				 * 
				 * TODO do we need this here?
				 */
//				headers.put("Origin", "moz-extension://");
				
				
				var form = parameters.entrySet()
					    .stream()
					    .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
					    .collect(Collectors.joining("&"));
				
				var client = HttpClient.newHttpClient();
				var tokenRequest = HttpRequest.newBuilder()
						.uri(new URI(c.getTokenUri()))
					    .headers("Content-Type", "application/x-www-form-urlencoded")
					    .POST(HttpRequest.BodyPublishers.ofString(form))
					    .build();
				var tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
				var json = tokenResponse.body().toString();
				var mapper = new ObjectMapper();
				var tokenResponseObj = mapper.readTree(json);
				
				if (tokenResponseObj.has("error")) {
					var err = tokenResponseObj.get("error").asText();
					var description = tokenResponseObj.has("error_description")
							? tokenResponseObj.get("error_description").asText()
							: null;
					throw new IllegalStateException(err + ". " + (description == null ? "" : " " + description));
				}

				var accessToken = tokenResponseObj.get("access_token").asText();
				var refreshToken = tokenResponseObj.has("refresh_token")
						? tokenResponseObj.get("refresh_token").asText()
						: null;
				var expires = System.currentTimeMillis() + (tokenResponseObj.get("expires_in").asInt() * 1000);
				var token = new OAuth2Token(accessToken, refreshToken, expires);

				c.handleAuthorization(token, request, response, c);
			} catch (Exception e) {
				LOG.error("Failed to complete authorization (" + c.getTokenUri() + ").", e);

				request.getSession().setAttribute("flashStyle", "danger");
				request.getSession().setAttribute("flash", e.getMessage());
				
				redirectTo = c.getBrowserUri();
			}

			response.sendRedirect(redirectTo);
		}
	}

	public void expectAuthorize(OAuth2Authorization auth) {
		synchronized (authorizations) {
			authorizations.put(auth.getState(), auth);
			// TODO expire the authorizations after certain amount time
		}
	}

	public static String genToken() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}

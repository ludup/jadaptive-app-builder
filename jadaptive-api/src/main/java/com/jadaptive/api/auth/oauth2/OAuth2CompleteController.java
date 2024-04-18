package com.jadaptive.api.auth.oauth2;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.auth.oauth2.OAuth2AuthorizationService.OAuth2Authorization;
import com.jadaptive.api.auth.oauth2.OAuth2AuthorizationService.OAuth2Token;
import com.jadaptive.api.ui.Feedback;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class OAuth2CompleteController {
	public static final String PATH_PREFIX = "/oauth2/end";
	
	static final Logger LOG = LoggerFactory.getLogger(OAuth2CompleteController.class);

	@Autowired
	private OAuth2AuthorizationService oAuth2AuthorizationService; 
	

	@RequestMapping(value = OAuth2CompleteController.PATH_PREFIX, method = RequestMethod.GET, produces = "text/plain")
	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	public void completeOAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
		synchronized (oAuth2AuthorizationService) {
			var state = request.getParameter("state");
			if (state == null) {
				LOG.error("No state parameter provided for oauth2 handler.");
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			var c = oAuth2AuthorizationService.get(state);
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

				Feedback.error("userInterface", "oauth2.completionFailed", e.getMessage());
				
				redirectTo = c.getBrowserUri();
			}

			response.sendRedirect(redirectTo);
		}
	}
}

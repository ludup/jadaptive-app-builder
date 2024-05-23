package com.jadaptive.api.auth.oauth2;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class OAuth2Request {
	
	public final static class Builder {
		
		private String codeChallenge;
		private String state;
		private String baseUri;
		private String redirectUri;
		private String clientId;
		private String userCode;
		private String clientSecret;
		private String codeVerifier;
		
		private String[] scopes;
		private String responseType ;
		private String grantType;
		private String nonce;
		private String codeChallengeMethod;

		public Builder forOAuthRequest(OAuth2Request request) {
			clientId = request.clientId;
			userCode = request.userCode;
			redirectUri = request.redirectUri;
			state = request.state;
			clientSecret = request.clientSecret;
			scopes = request.scopes;
			responseType = request.responseType;
			grantType = request.grantType;
			nonce= request.nonce;
			codeChallenge = request.codeChallenge;
			codeChallengeMethod = request.codeChallengeMethod;
			return this;
		}
		
		public Builder forRequest(HttpServletRequest req) {
			clientId = req.getParameter("client_id");
			redirectUri = req.getParameter("redirect_uri");
			state = req.getParameter("state");
			clientSecret = req.getParameter("client_secret");
			scopes = req.getParameterValues("scope");
			responseType = req.getParameter("response_type");
			grantType = req.getParameter("grant_type");
			nonce= req.getParameter("nonce");
			codeChallenge = req.getParameter("code_challenge");
			codeChallengeMethod = req.getParameter("code_challenge_method");
			return this;
		}
		
		public Builder withCodeChallenge(String codeChallenge) {
			this.codeChallenge = codeChallenge;
			return this;
		}


		public Builder withState() {
			return withState(OAuth2AuthorizationService.genToken());
		}

		public Builder withState(String state) {
			this.state = state;
			return this;
		}

		public Builder withBaseUri(String baseUri) {
			this.baseUri = baseUri;
			return this;
		}
		
		public Builder withPKE() {
			try {
				
				codeVerifier = OAuth2AuthorizationService.genToken(); 
				
				MessageDigest digest = MessageDigest.getInstance("SHA256");
				digest.reset();
				digest.update(codeVerifier.getBytes("UTF-8"));
				byte[] hashed = digest.digest();
				codeChallenge = Base64.getEncoder().encodeToString(hashed);
			}
			catch(NoSuchAlgorithmException | UnsupportedEncodingException  nsae) {
				throw new IllegalStateException("Failed to create OAuth2 request.", nsae);
			}
			return this;
		}

		public Builder withRedirectUri(HttpServletRequest req, String serverBasePath) {
			try {
				URL requestURL = new URL(req.getRequestURL().toString());
				redirectUri =  requestURL.getProtocol() + "://" + 
						requestURL.getHost() + ( requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort() ) + serverBasePath + OAuth2CompleteController.PATH_PREFIX;
				return this;
			}
			catch(MalformedURLException murle) {
				throw new IllegalArgumentException("Invalid redirect uri.", murle);
			}
		}

		public Builder withRedirectUri(String redirectUri) {
			this.redirectUri = redirectUri;
			return this;
		}

		public Builder withClientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public Builder withUserCode(String userCode) {
			this.userCode = userCode;
			return this;
		}

		public Builder withClientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
			return this;
		}

		public Builder withCodeVerifier(String codeVerifier) {
			this.codeVerifier = codeVerifier;
			return this;
		}

		public Builder withScopes(String[] scopes) {
			this.scopes = scopes;
			return this;
		}

		public Builder withResponseType(String responseType) {
			this.responseType = responseType;
			return this;
		}

		public Builder withGrantType(String grantType) {
			this.grantType = grantType;
			return this;
		}

		public Builder withNonce(String nonce) {
			this.nonce = nonce;
			return this;
		}

		public Builder withCodeChallengeMethod(String codeChallengeMethod) {
			this.codeChallengeMethod = codeChallengeMethod;
			return this;
		}

		public OAuth2Request build() {
			return new OAuth2Request(this);
		}
	}
	
	private final String codeChallenge;
	private final String state;
	private final String baseUri;
	private final String redirectUri;
	private final String clientId;
	private final String clientSecret;
	private final String codeVerifier;
	private final String userCode;
	
	private final String[] scopes;
	private final String responseType;
	private final String grantType;
	private final String nonce;
	private final String codeChallengeMethod;
	
	private OAuth2Request(Builder builder) {
		this.codeChallenge = builder.codeChallenge;
		this.state = builder.state;
		this.userCode = builder.userCode;
		this.baseUri = builder.baseUri;
		this.redirectUri = builder.redirectUri;
		this.clientId = builder.clientId;
		this.clientSecret = builder.clientSecret;
		this.codeVerifier = builder.codeVerifier;
		this.scopes = builder.scopes;
		this.responseType = builder.responseType;
		this.grantType = builder.grantType;
		this.nonce = builder.nonce;
		this.codeChallengeMethod = builder.codeChallengeMethod;
	}

	public String uri(String... scopes) { 
		try {
			var clientIdTxt = clientId == null ? "" : ( 
					"client_id=" + URLEncoder.encode(clientId, "UTF-8") + "&"
			);
			if(clientSecret != null)  {  
				clientIdTxt  += "client_secret=" + URLEncoder.encode(clientSecret, "UTF-8") + "&";
			}
			
			var res = baseUri + 
					"/oauth2-start?" + clientIdTxt +
					"redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");
			
			if(responseType != null)
				res += "&response_type=" + responseType;
			
			if(grantType != null)
				res += "&grant_type=" + grantType;
			
			if(state != null) {
				res += "&state=" + URLEncoder.encode(state, "UTF-8");
			}
			
			for(var scope : scopes) {
				res += "&scope=" + URLEncoder.encode(scope, "UTF-8");
			}
			
			if(codeChallenge != null) {
				res += "&code_challenge=" +  URLEncoder.encode(codeChallenge, "UTF-8") + 
					   "&code_challenge_method=S256";
			}
			
			return res;
		}
		catch(UnsupportedEncodingException uee) {
			throw new IllegalStateException(uee);
		}
	}

	public String userCode() {
		return userCode;
	}

	public String requestedNonce() {
		return nonce;
	}

	public String requestedCodeChallenge() {
		return codeChallenge;
	}

	public String requestedCodeChallengeMethod() {
		return codeChallengeMethod;
	}

	public String requestedResponseType() {
		return responseType;
	}

	public String requestedGrantType() {
		return grantType;
	}

	public String[] requestedScopes() {
		if(scopes == null)
			throw new IllegalStateException("Cannot get requested scopes on server side.");
		return scopes;
	}

	public String codeVerifier() {
		return codeVerifier;
	}

	public String state() {
		return state;
	}

	public String redirectUri() {
		return redirectUri;
	}

	public String clientId() {
		return clientId;
	}

	public String baseUri() {
		return baseUri;
	}

	public static void set(HttpSession state, OAuth2Request request) {
		state.setAttribute(OAuth2Request.class.getName(), request);
	}

	public static Optional<OAuth2Request> get(HttpSession state) {
		return Optional.ofNullable((OAuth2Request)state.getAttribute(OAuth2Request.class.getName()));
	}

}
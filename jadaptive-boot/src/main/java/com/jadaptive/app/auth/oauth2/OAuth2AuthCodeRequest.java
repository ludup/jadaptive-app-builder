package com.jadaptive.app.auth.oauth2;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.jadaptive.api.auth.oauth2.OAuth2AuthorizationService;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.user.User;

public class OAuth2AuthCodeRequest implements Serializable {
	
	private static final long serialVersionUID = 370849847916918626L;
	
	private String token;
	private Set<String> scopes;
	private User principal;
	private String nonce;
	private String codeChallenge;
	private String codeChallengeMethod;
	
	public OAuth2AuthCodeRequest() {
		token = OAuth2AuthorizationService.genToken();
		scopes = new LinkedHashSet<>();
	}

	public OAuth2AuthCodeRequest(Set<OAuth2Scope> scopes, String redirectUri, User principal, String nonce, String codeChallenge, String codeChallengeMethod) {
		token = OAuth2AuthorizationService.genToken();
		this.scopes = scopes.stream().map(s -> s.getId()).sorted().collect(Collectors.toSet());
		this.codeChallengeMethod = codeChallengeMethod == null ? "plain" : codeChallengeMethod;
		this.principal = principal;
		this.nonce = nonce;
		this.codeChallenge = codeChallenge;
	}
	
	public String[] getScopeNames() {
		return scopes.toArray(new String[0]);
	}
	
	public String getCodeChallengeMethod() {
		return codeChallengeMethod;
	}

	public String getCodeChallenge() {
		return codeChallenge;
	}

	public String getNonce() {
		return nonce;
	}

	public User getPrincipal() {
		return principal;
	}

	public String getAuthCode() {
		return token;
	}

}

package com.jadaptive.app.auth.oauth2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.jadaptive.api.auth.oauth2.OAuth2AuthorizationService;
import com.jadaptive.api.auth.oauth2.OAuth2Scope;
import com.jadaptive.api.user.User;

public class OAuth2AuthCodeRequest {
	
	private final String token = OAuth2AuthorizationService.genToken();
	private final Set<OAuth2Scope> scopes;
	private final User principal;
	private final String nonce;
	private final String codeChallenge;
	private final String codeChallengeMethod;

	public OAuth2AuthCodeRequest(Set<OAuth2Scope> scopes, String redirectUri, User principal, String nonce, String codeChallenge, String codeChallengeMethod) {
		this.scopes = scopes;
		this.codeChallengeMethod = codeChallengeMethod == null ? "plain" : codeChallengeMethod;
		this.principal = principal;
		this.nonce = nonce;
		this.codeChallenge = codeChallenge;
	}
	
	public String[] getScopeNames() {
		List<String> n = new ArrayList<>();
		for(OAuth2Scope scope : scopes)
			n.add(scope.getId());
		Collections.sort(n);
		return n.toArray(new String[0]);
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

	public Set<OAuth2Scope> getScopes() {
		return scopes;
	}

}

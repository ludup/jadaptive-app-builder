package com.jadaptive.api.auth.oauth2;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

	private Map<String, OAuth2Authorization> authorizations = new HashMap<>();

	@Override
	public void expectAuthorize(OAuth2Authorization auth) {
		authorizations.put(auth.getState(), auth);
	}

	@Override
	public OAuth2Authorization get(String state) {
		return authorizations.get(state);
	}
}

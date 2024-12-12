package com.jadaptive.api.auth.oauth2;

import java.util.Optional;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.user.User;

public interface OAuth2Scope extends ExtensionPoint {

	String getId();

	default String getBundle() {
		return "default";
	}
	
	default Strictness getStrictness() {
		return Strictness.STRICT;
	}
	
	default void verifyPermissions(Optional<OAuth2Request> oauthRequest, User principal) throws AccessDeniedException {
	}

	default void onApproved(OAuth2Request oauthRequest) {
	}

	default void onRejected(OAuth2Request oauthRequest) {
	}
	
	default String decorateVerificationUri(OAuth2Request oauthRequest, String uri) {
		return uri;
	}
}

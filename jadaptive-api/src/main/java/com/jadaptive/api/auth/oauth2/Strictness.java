package com.jadaptive.api.auth.oauth2;

import java.util.Arrays;
import java.util.Collection;

public enum Strictness {
	/**
	 * Will NOT ask for user code confirmation if required and supplied, will NOT
	 * ask for scope approval
	 */
	LAX,
	/**
	 * Will NOT ask for user code confirmation, but WILL ask for scope approval.
	 */
	MODERATE,
	/**
	 * WILL ask for user code confirmation, and WILL ask for scope approval
	 */
	STRICT;
	
	
	public static Strictness forScopes(OAuth2Scope... scopes) {
		return forScopes(Arrays.asList(scopes));
	}
	
	public static Strictness forScopes(Collection<OAuth2Scope> scopes) {
		if(scopes.isEmpty())
			return Strictness.STRICT;
		else {
			var strictest = Strictness.LAX;
			for(var scope : scopes) {
				var scoStr = scope.getStrictness();
				if(strictest.ordinal() < scoStr.ordinal()) {
					strictest = scoStr;
				}
			}
			return strictest;
		}
	}
}

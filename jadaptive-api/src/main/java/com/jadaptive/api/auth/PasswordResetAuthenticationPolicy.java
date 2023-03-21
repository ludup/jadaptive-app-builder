package com.jadaptive.api.auth;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(bundle = AuthenticationPolicy.RESOURCE_KEY, resourceKey = PasswordResetAuthenticationPolicy.RESOURCE_KEY, scope = ObjectScope.GLOBAL, defaultColumn = "name")
@ObjectServiceBean(bean = AuthenticationPolicyService.class)
@ObjectViews({})
@GenerateEventTemplates(PasswordResetAuthenticationPolicy.RESOURCE_KEY)
public class PasswordResetAuthenticationPolicy extends AuthenticationPolicy {

	private static final long serialVersionUID = -3171397241055913686L;

	public static final String RESOURCE_KEY = "passwordResetPolicy";
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public Boolean getPasswordOnFirstPage() {
		return Boolean.FALSE;
	}

	@Override
	public AuthenticationScope getScope() {
		return AuthenticationScope.PASSWORD_RESET;
	}

	@Override
	public boolean isSessionRequired() {
		return false;
	}

}

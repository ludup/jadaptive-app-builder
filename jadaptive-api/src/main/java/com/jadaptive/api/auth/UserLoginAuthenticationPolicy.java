package com.jadaptive.api.auth;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(bundle = AuthenticationPolicy.RESOURCE_KEY, resourceKey = UserLoginAuthenticationPolicy.RESOURCE_KEY, scope = ObjectScope.GLOBAL, defaultColumn = "name")
@ObjectServiceBean(bean = AuthenticationPolicyService.class)
@ObjectViews({})
@GenerateEventTemplates(UserLoginAuthenticationPolicy.RESOURCE_KEY)
public class UserLoginAuthenticationPolicy extends AuthenticationPolicy {

	private static final long serialVersionUID = -4581883248747380399L;

	public static final String RESOURCE_KEY = "userLoginPolicy";
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "true")
	@ObjectView(value = "factors")
	Boolean passwordOnFirstPage;
	
	public Boolean getPasswordOnFirstPage() {
		return passwordOnFirstPage;
	}

	public void setPasswordOnFirstPage(Boolean passwordOnFirstPage) {
		this.passwordOnFirstPage = passwordOnFirstPage;
	}

	@Override
	public AuthenticationScope getScope() {
		return AuthenticationScope.USER_LOGIN;
	}

	@Override
	public boolean isSessionRequired() {
		return true;
	}
}

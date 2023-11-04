package com.jadaptive.api.auth;

import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;

//@ObjectDefinition(bundle = AuthenticationPolicy.RESOURCE_KEY, resourceKey = SAMLIdpAuthenticationPolicy.RESOURCE_KEY, scope = ObjectScope.GLOBAL, defaultColumn = "name")
//@ObjectServiceBean(bean = AuthenticationPolicyService.class)
//@ObjectViews({})
//@GenerateEventTemplates(SAMLIdpAuthenticationPolicy.RESOURCE_KEY)
public class SAMLIdpAuthenticationPolicy extends AuthenticationPolicy {

	private static final long serialVersionUID = -3171397241055913686L;

	public static final String RESOURCE_KEY = "samlIdpPolicy";
	
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
	public boolean isSessionRequired() {
		return false;
	}

}

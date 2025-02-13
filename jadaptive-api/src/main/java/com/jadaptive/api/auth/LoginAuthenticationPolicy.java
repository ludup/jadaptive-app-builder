package com.jadaptive.api.auth;

import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;

public abstract class LoginAuthenticationPolicy extends AuthenticationPolicy {

	private static final long serialVersionUID = 1750581295152966131L;

	@ObjectField(type = FieldType.BOOL, defaultValue = "true")	
	@ObjectView(value = "optional", weight = -1000)
	Boolean ensureOptionalSetup;

	public Boolean getEnsureOptionalSetup() {
		return ensureOptionalSetup;
	}

	public void setEnsureOptionalSetup(Boolean ensureOptionalSetup) {
		this.ensureOptionalSetup = ensureOptionalSetup;
	}
}

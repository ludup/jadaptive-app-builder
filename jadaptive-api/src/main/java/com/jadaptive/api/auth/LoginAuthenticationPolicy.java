package com.jadaptive.api.auth;

import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectViewDefinition;

@ObjectViewDefinition(bundle = AuthenticationPolicy.RESOURCE_KEY, value = "bannerView")
public abstract class LoginAuthenticationPolicy extends AuthenticationPolicy {

	private static final long serialVersionUID = 1750581295152966131L;

	@ObjectField(type = FieldType.TEXT_AREA, view = "bannerView", renderer = FieldRenderer.HTML_EDITOR)
	String banner;

	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}
}

package com.jadaptive.api.auth.oauth2;

import java.util.Collection;

import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.TableView;
import com.jadaptive.api.template.ValidationType;
import com.jadaptive.api.template.Validator;

/**
 * NOTE: 
 * 
 * These are hidden for now. We *should* be using applications to restrict who can use OAuth
 * facilities. But these don't really offer any real protection unless client_secrets are
 * also used. This would introduce additional unwanted setup for our use cases unless we
 * embedded fixed client_id and client_secrets in each product. This needs discussion.
 */
@ObjectDefinition(bundle = OAuth2Application.RESOURCE_KEY, resourceKey = OAuth2Application.RESOURCE_KEY, creatable = true, updatable = true, deletable = true)
@TableView(defaultColumns = { "name" })
@ObjectViewDefinition(value = OAuth2Application.VIEW_APPLICATION, weight = 100)
@ObjectServiceBean(bean = OAuth2ApplicationService.class)
@GenerateEventTemplates
//@PageMenu(parent = ApplicationMenuService.SECURITY_MENU_UUID, icon = "fa-handshake-simple", weight = 999, withPermission = "oauth2Applications.readWrite")
public class OAuth2Application extends NamedUUIDEntity {

	private static final long serialVersionUID = -7288600405384404678L;
	static final String RESOURCE_KEY = "oauth2Applications";

	public static final String VIEW_APPLICATION = "applicationView";

	@ObjectField(type = FieldType.PASSWORD, defaultValue = "", automaticEncryption = true)
	@ObjectView(VIEW_APPLICATION)
	private String secret;

	@ObjectField(type = FieldType.TEXT)
	@ObjectView(VIEW_APPLICATION)
	@Validator(type = ValidationType.CIDR_V4)
	@Validator(type = ValidationType.CIDR_V6)
	private Collection<String> redirectUris;

	@ObjectView(value = VIEW_APPLICATION)
	@ObjectField(type = FieldType.ENUM, readOnly = true)
	private GrantType grantType;

	@ObjectField(type = FieldType.LONG)
	@ObjectView(VIEW_APPLICATION)
	private long expiryTime;

	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(VIEW_APPLICATION)
	private boolean issueRefreshToken;

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public boolean isIssueRefreshToken() {
		return issueRefreshToken;
	}

	public void setIssueRefreshToken(boolean issueRefreshToken) {
		this.issueRefreshToken = issueRefreshToken;
	}

	public long getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}

	public GrantType getGrantType() {
		return grantType;
	}

	public void setGrantType(GrantType grantType) {
		this.grantType = grantType;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Collection<String> getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(Collection<String> redirectUris) {
		this.redirectUris = redirectUris;
	}

}

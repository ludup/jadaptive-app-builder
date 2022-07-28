package com.jadaptive.api.auth;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@ObjectDefinition(resourceKey = AuthenticationModule.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
public class AuthenticationModule extends NamedUUIDEntity {

	private static final long serialVersionUID = -2303774620847729028L;

	public static final String RESOURCE_KEY = "authenticationModule";

	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	Boolean enabled;
	
	@ObjectField(searchable = true, unique = true, type = FieldType.TEXT)
	String authenticatorKey;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	boolean identityCapture;
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	boolean secretCapture;
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public String getAuthenticatorKey() {
		return authenticatorKey;
	}

	public void setAuthenticatorKey(String authenticatorKey) {
		this.authenticatorKey = authenticatorKey;
	}

	public boolean isIdentityCapture() {
		return identityCapture;
	}

	public void setIdentityCapture(boolean identityCapture) {
		this.identityCapture = identityCapture;
	}

	public boolean isSecretCapture() {
		return secretCapture;
	}

	public void setSecretCapture(boolean secretCapture) {
		this.secretCapture = secretCapture;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	

}

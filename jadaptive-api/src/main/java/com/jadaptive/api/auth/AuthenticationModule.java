package com.jadaptive.api.auth;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = AuthenticationModule.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
@TableView(defaultColumns = "name")
public class AuthenticationModule extends NamedUUIDEntity {

	private static final long serialVersionUID = -2303774620847729028L;

	public static final String RESOURCE_KEY = "authenticationModule";

	@ObjectField(searchable = true, unique = true, type = FieldType.TEXT)
	String authenticatorKey;

	public String getAuthenticatorKey() {
		return authenticatorKey;
	}

	public void setAuthenticatorKey(String authenticatorKey) {
		this.authenticatorKey = authenticatorKey;
	}

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	
}

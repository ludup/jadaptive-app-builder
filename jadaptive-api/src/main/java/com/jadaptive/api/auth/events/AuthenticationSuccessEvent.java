package com.jadaptive.api.auth.events;

import com.jadaptive.api.auth.AuthenticationModule;
import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.events.UserGeneratedEvent;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@AuditedObject
@ObjectDefinition(resourceKey = AuthenticationSuccessEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = Session.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
@ObjectViews({@ObjectViewDefinition(bundle = Session.RESOURCE_KEY, value = ObjectEvent.OBJECT_VIEW)})
public class AuthenticationSuccessEvent extends UserGeneratedEvent {

	private static final long serialVersionUID = -6350681450369361249L;

	public static final String RESOURCE_KEY = "authenticationSuccess";

	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = AuthenticationModule.RESOURCE_KEY)
	@ObjectView(value = ObjectEvent.OBJECT_VIEW)
	AuthenticationModule authenticationModule;
	
	public AuthenticationSuccessEvent() { }
	
	public AuthenticationSuccessEvent(AuthenticationModule authenticationModule, String username, String name, String remoteAddress) {
		super(RESOURCE_KEY, "authentication");
		setUsername(username);
		setName(name);
		setIpAddress(remoteAddress);
		setEventDescription(authenticationModule.getName());
		this.authenticationModule = authenticationModule;
	}

	public AuthenticationModule getAuthenticationModule() {
		return authenticationModule;
	}

	public void setAuthenticationModule(AuthenticationModule authenticationModule) {
		this.authenticationModule = authenticationModule;
	}

	
}

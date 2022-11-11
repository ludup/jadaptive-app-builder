package com.jadaptive.api.session.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.events.UserGeneratedEvent;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;

@AuditedObject
@ObjectDefinition(resourceKey = SessionOpenedEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = Session.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
public class SessionOpenedEvent extends UserGeneratedEvent {

	private static final long serialVersionUID = -6350681450369361249L;

	public static final String RESOURCE_KEY = "sessionOpened";
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = SystemEvent.EVENT_VIEW, weight = 9999, bundle = Session.RESOURCE_KEY)
	String userAgent;
	
	public SessionOpenedEvent(Session object) {
		super(RESOURCE_KEY, "sessions");
		setUsername(object.getUser().getUsername());
		setName(object.getUser().getName());
		this.userAgent = object.getUserAgent();
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
}

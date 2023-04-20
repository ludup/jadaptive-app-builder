package com.jadaptive.api.session.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.events.UserGeneratedEvent;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@AuditedObject
@ObjectDefinition(resourceKey = SessionClosedEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = Session.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
@ObjectViews({@ObjectViewDefinition(bundle = Session.RESOURCE_KEY, value = ObjectEvent.OBJECT_VIEW)})
public class SessionClosedEvent extends UserGeneratedEvent {

	private static final long serialVersionUID = -6350681450369361249L;

	public static final String RESOURCE_KEY = "sessionClosed";

	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = SystemEvent.EVENT_VIEW, weight = 9999, bundle = Session.RESOURCE_KEY)
	String userAgent;

	public SessionClosedEvent() {
		super(RESOURCE_KEY, "sessions");
	}
	
	public SessionClosedEvent(Session object) {
		super(RESOURCE_KEY, "sessions");
		setUsername(object.getUser().getUsername());
		setName(object.getUser().getName());
		setIpAddress(object.getRemoteAddress());
		this.userAgent = object.getUserAgent();
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	
}

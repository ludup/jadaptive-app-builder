package com.jadaptive.api.session.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.events.ObjectEvent;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@AuditedObject
@ObjectDefinition(resourceKey = SessionOpenedEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT, bundle = Session.RESOURCE_KEY)
@ObjectViews({@ObjectViewDefinition(bundle = Session.RESOURCE_KEY, value = ObjectEvent.OBJECT_VIEW)})
public class SessionOpenedEvent extends SessionEvent {

	private static final long serialVersionUID = -6350681450369361249L;

	public static final String RESOURCE_KEY = "session.opened";
	
	public SessionOpenedEvent(Session session) {
		super(RESOURCE_KEY, session);
	}

}

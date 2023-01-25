package com.jadaptive.api.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = UserGeneratedEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT, creatable = false, updatable = false, deletable = false)
public abstract class UserGeneratedEvent extends SystemEvent {

	private static final long serialVersionUID = 6015292582999672923L;

	public static final String RESOURCE_KEY = "objectEvent";

	public UserGeneratedEvent(String resourceKey, String group) {
		super(resourceKey, group);
	}
	
	public UserGeneratedEvent(String resourceKey, String group, Throwable e) {
		super(resourceKey, group, e);
	}
	
	protected void onSessionAttach(Session session) {
		this.name = session.getUser().getName();
		this.username = session.getUser().getUsername();
	}

	@Override
	public boolean async() { return false; }

}

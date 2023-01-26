package com.jadaptive.api.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.template.ObjectDefinition;

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

	@Override
	public boolean async() { return false; }

}

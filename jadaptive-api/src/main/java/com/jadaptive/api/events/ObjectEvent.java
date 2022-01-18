package com.jadaptive.api.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = ObjectEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
@ObjectViews({@ObjectViewDefinition(value = "object", bundle = SystemEvent.RESOURCE_KEY)})
public abstract class ObjectEvent<T extends UUIDEntity> extends SystemEvent {

	private static final long serialVersionUID = 6015292582999672923L;

	public static final String RESOURCE_KEY = "objectEvent";
	
	public static final String OBJECT_VIEW = "object";
	
	public ObjectEvent(String resourceKey, String group) {
		super(resourceKey, group);
	}
	
	public ObjectEvent(String resourceKey, String group, Throwable e) {
		super(resourceKey, group, e);
	}
	
	public abstract T getObject();
	
	@Override
	public boolean async() { return false; };

	public String getEventGroup() {
		return getObject().getEventGroup();
	}
}

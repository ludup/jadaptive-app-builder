package com.jadaptive.api.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = ObjectUpdateEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT, creatable = false, updatable = false, deletable = false)
@ObjectViews({@ObjectViewDefinition(value = "object", bundle = SystemEvent.RESOURCE_KEY),
	@ObjectViewDefinition(value = "previous", bundle = SystemEvent.RESOURCE_KEY)})
public abstract class ObjectUpdateEvent<T extends UUIDEntity> extends ObjectEvent<T> {

	private static final long serialVersionUID = 6015292582999672923L;

	public static final String RESOURCE_KEY = "objectUpdateEvent";
	
	public static final String OBJECT_VIEW = "object";
	public static final String PREVIOUS_VIEW = "previous";

	public ObjectUpdateEvent() {	
	}
	
	public ObjectUpdateEvent(String resourceKey, String group) {
		super(resourceKey, group);
	}
	
	public ObjectUpdateEvent(String resourceKey, String group, Throwable e) {
		super(resourceKey, group, e);
	}

	public abstract T getPrevious();
	
}

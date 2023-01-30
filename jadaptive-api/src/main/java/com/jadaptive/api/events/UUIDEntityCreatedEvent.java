package com.jadaptive.api.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = UUIDEntityCreatedEvent.RESOURCE_KEY, 
		scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, 
		creatable = false, 
		updatable = false, 
		deletable = false)
@ObjectViews({@ObjectViewDefinition(value = "object", bundle = SystemEvent.RESOURCE_KEY)})
public class UUIDEntityCreatedEvent<T extends UUIDEntity> extends ObjectEvent<T> {

	public static final String RESOURCE_KEY = "uuid.created";
	
	private static final long serialVersionUID = 6015292582999672923L;

	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	T object;
	
	public UUIDEntityCreatedEvent(T object) {
		super(Events.created(object.getEventGroup()), object.getEventGroup());
		this.object = object;
		if(object instanceof NamedDocument) {
			setEventDescription(((NamedDocument)object).getName());
		}
	}
	
	public UUIDEntityCreatedEvent(T object, Throwable e) {
		super(Events.created(object.getEventGroup()), object.getEventGroup(), e);
		this.object = object;
		if(object instanceof NamedDocument) {
			setEventDescription(((NamedDocument)object).getName());
		}
	}

	@Override
	public T getObject() {
		return object;
	}
}

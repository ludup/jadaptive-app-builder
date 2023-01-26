package com.jadaptive.api.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = UUIDEntityUpdatedEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT)
@ObjectViews({@ObjectViewDefinition(value = UUIDEntityUpdatedEvent.PREVIOUS_VIEW, bundle = SystemEvent.RESOURCE_KEY)})
public class UUIDEntityUpdatedEvent<T extends UUIDEntity> extends ObjectEvent<T> {

	private static final long serialVersionUID = 6015292582999672923L;

	public static final String RESOURCE_KEY = "objectUpdated";
	public static final String PREVIOUS_VIEW = "previousObject";
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	@ObjectView(OBJECT_VIEW)
	T object;
	
	@ObjectField(type = FieldType.OBJECT_EMBEDDED)
	@ObjectView(PREVIOUS_VIEW)
	T previousObject;
	
	public UUIDEntityUpdatedEvent(T object, T previousObject) {
		super(Events.updated(object.getEventGroup()), object.getEventGroup());
		this.object = object;
		if(object instanceof NamedUUIDEntity) {
			setEventDescription(((NamedUUIDEntity)object).getName());
		}
		this.previousObject = previousObject;
	}
	
	public UUIDEntityUpdatedEvent(T object, T previousObject, Throwable t) {
		super(Events.updated(object.getEventGroup()), object.getEventGroup(), t);
		this.object = object;
		if(object instanceof NamedUUIDEntity) {
			setEventDescription(((NamedUUIDEntity)object).getName());
		}
		this.previousObject = previousObject;
	}
	
	public T getPreviousObject() {
		return previousObject;
	}

	@Override
	public T getObject() {
		return object;
	}

}

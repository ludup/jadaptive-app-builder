package com.jadaptive.api.events;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.template.FieldRenderer;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectDefinition(resourceKey = ObjectEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT, creatable = false, updatable = false, deletable = false)
@ObjectViews({@ObjectViewDefinition(value = "object", bundle = SystemEvent.RESOURCE_KEY)})
public abstract class ObjectEvent<T extends UUIDEntity> extends SystemEvent {

	private static final long serialVersionUID = 6015292582999672923L;

	public static final String RESOURCE_KEY = "objectEvent";
	
	public static final String OBJECT_VIEW = "object";
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = SystemEvent.EVENT_VIEW, weight = 9997, bundle = Session.RESOURCE_KEY, renderer = FieldRenderer.OPTIONAL)
	String username;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = SystemEvent.EVENT_VIEW, weight = 9998, bundle = Session.RESOURCE_KEY, renderer = FieldRenderer.OPTIONAL)
	String name;
	
	public ObjectEvent(String resourceKey, String group) {
		super(resourceKey, group);
	}
	
	public ObjectEvent(String resourceKey, String group, Throwable e) {
		super(resourceKey, group, e);
	}
	
	protected void onSessionAttach(Session session) {
		super.onSessionAttach(session);
		this.name = session.getUser().getName();
		this.username = session.getUser().getUsername();
	}

	public abstract T getObject();
	
	@Override
	public boolean async() { return false; }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	};

}

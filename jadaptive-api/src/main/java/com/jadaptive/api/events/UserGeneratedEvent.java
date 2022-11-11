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

@ObjectDefinition(resourceKey = UserGeneratedEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.OBJECT, creatable = false, updatable = false, deletable = false)
@ObjectViews({@ObjectViewDefinition(value = UserGeneratedEvent.CURRENT_USER, bundle = SystemEvent.RESOURCE_KEY, weight = Integer.MIN_VALUE+1)})
public abstract class UserGeneratedEvent extends SystemEvent {

	private static final long serialVersionUID = 6015292582999672923L;

	public static final String RESOURCE_KEY = "objectEvent";

	public static final String CURRENT_USER = "currentUserView";
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = CURRENT_USER, weight = 9997, bundle = Session.RESOURCE_KEY, renderer = FieldRenderer.OPTIONAL)
	String username;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(value = CURRENT_USER, weight = 9998, bundle = Session.RESOURCE_KEY, renderer = FieldRenderer.OPTIONAL)
	String name;
	
	public UserGeneratedEvent(String resourceKey, String group) {
		super(resourceKey, group);
	}
	
	public UserGeneratedEvent(String resourceKey, String group, Throwable e) {
		super(resourceKey, group, e);
	}
	
	protected void onSessionAttach(Session session) {
		super.onSessionAttach(session);
		this.name = session.getUser().getName();
		this.username = session.getUser().getUsername();
	}

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

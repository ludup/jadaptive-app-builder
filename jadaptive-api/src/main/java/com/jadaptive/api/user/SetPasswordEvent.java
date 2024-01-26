package com.jadaptive.api.user;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@AuditedObject
@ObjectDefinition(resourceKey = SetPasswordEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = User.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
@ObjectViews(@ObjectViewDefinition(bundle = User.RESOURCE_KEY, value = SetPasswordEvent.TARGET_VIEW))
public class SetPasswordEvent extends PasswordEvent {

	private static final long serialVersionUID = 5458426923519872724L;
	public static final String RESOURCE_KEY = "setPassword";
	
	public static final String TARGET_VIEW = "setPasswordTarget";
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(TARGET_VIEW)
	String targetUsername;
	
	@ObjectField(type = FieldType.TEXT)
	@ObjectView(TARGET_VIEW)
	String targetName;
	
	public SetPasswordEvent() { 
		super(RESOURCE_KEY);
	}
	
	public SetPasswordEvent(User user, Throwable t) {
		super(RESOURCE_KEY, t);
		setTargetUsername(user.getUsername());
		setTargetName(user.getName());
	}
	
	public SetPasswordEvent(User user) {
		super(RESOURCE_KEY);
		setEventDescription(user.getUsername());
		setTargetUsername(user.getUsername());
		setTargetName(user.getName());
	}

	public String getTargetUsername() {
		return targetUsername;
	}

	public void setTargetUsername(String targetUsername) {
		this.targetUsername = targetUsername;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
}

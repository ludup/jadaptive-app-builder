package com.jadaptive.app.user;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.user.User;

@AuditedObject
@ObjectDefinition(resourceKey = ChangePasswordEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = User.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
public class ChangePasswordEvent extends PasswordEvent {

	private static final long serialVersionUID = 5458426923519872724L;
	public static final String RESOURCE_KEY = "changePassword";
	
	public ChangePasswordEvent(Throwable t) {
		super(RESOURCE_KEY, t);
	}
	
	public ChangePasswordEvent() {
		super(RESOURCE_KEY);
	}

}

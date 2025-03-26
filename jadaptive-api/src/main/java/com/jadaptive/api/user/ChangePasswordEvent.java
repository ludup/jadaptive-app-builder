package com.jadaptive.api.user;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.AuditedObject;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;

@AuditedObject
@ObjectDefinition(resourceKey = ChangePasswordEvent.RESOURCE_KEY, scope = ObjectScope.GLOBAL, 
		type = ObjectType.OBJECT, bundle = User.RESOURCE_KEY,
			creatable = false, updatable = false, deletable = false)
public class ChangePasswordEvent extends PasswordEvent {

	private static final long serialVersionUID = 5458426923519872724L;
	public static final String RESOURCE_KEY = "changePassword";
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	String previousPasswordPartHash;
	
	@ObjectField(type = FieldType.TEXT, hidden = true)
	String currentPasswordPartHash;
	
	public ChangePasswordEvent(Throwable t) {
		super(RESOURCE_KEY, t);
	}
	
	public ChangePasswordEvent(String previousPasswordPartHash, String currentPasswordPartHash) {
		super(RESOURCE_KEY);
	}

	public String getPreviousPasswordPartHash() {
		return previousPasswordPartHash;
	}

	public void setPreviousPasswordPartHash(String previousPasswordPartHash) {
		this.previousPasswordPartHash = previousPasswordPartHash;
	}

	public String getCurrentPasswordPartHash() {
		return currentPasswordPartHash;
	}

	public void setCurrentPasswordPartHash(String currentPasswordPartHash) {
		this.currentPasswordPartHash = currentPasswordPartHash;
	}

	
}

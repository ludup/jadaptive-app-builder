package com.jadaptive.api.repository;

import java.util.Objects;

import com.jadaptive.api.app.App;
import com.jadaptive.api.template.FieldOptions;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

public abstract class PersonalUUIDEntity extends AbstractUUIDEntity {

	private static final long serialVersionUID = -8870385785883925751L;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = User.RESOURCE_KEY, hidden = true, options = FieldOptions.CASCADE_ON_DELETED_REFERENCE)
	private User owner;
	
	private String ownerUUID;

	public String getOwnerUUID() {
		return ownerUUID;
	}

	public void setOwnerUUID(String ownerUUID) {
		this.ownerUUID = ownerUUID;
	}

	public User getOwner() {
		if(Objects.nonNull(owner)) {
			return owner = App.bean(UserService.class).getObjectByUUID(ownerUUID);
		}
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
		this.ownerUUID = owner.getUuid();
	}
}

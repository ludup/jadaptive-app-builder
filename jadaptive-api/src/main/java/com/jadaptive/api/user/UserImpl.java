package com.jadaptive.api.user;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = "users", type = ObjectType.COLLECTION)
public abstract class UserImpl extends UUIDEntity implements User {

	private static final long serialVersionUID = 2210375165051752363L;

}

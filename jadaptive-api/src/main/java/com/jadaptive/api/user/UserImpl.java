package com.jadaptive.api.user;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.Template;

@Template(name = "Users", resourceKey = "users", type = ObjectType.COLLECTION)
public abstract class UserImpl extends UUIDEntity implements User {

}

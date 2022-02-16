package com.jadaptive.api.user;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = "users", type = ObjectType.COLLECTION, defaultColumn = "username")
@TableView(defaultColumns = { "username", "name"})
public abstract class UserImpl extends UUIDEntity implements User {

	private static final long serialVersionUID = 2210375165051752363L;

	@ObjectField(required = true,
			searchable = true,
			type = FieldType.TEXT, 
			unique = true)
	String username;
	
	@ObjectField(required = true,
			searchable = true,
			nameField = true,
			type = FieldType.TEXT)
	String name;
	
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
	}

	@Override
	public String getSystemName() {
		return getName();
	}
}

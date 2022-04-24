package com.jadaptive.api.user;

import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedDocument;
import com.jadaptive.api.repository.UUIDEntity;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectServiceBean;
import com.jadaptive.api.template.TableAction;
import com.jadaptive.api.template.TableAction.Target;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = "users", type = ObjectType.COLLECTION, defaultColumn = "username")
@ObjectServiceBean(bean = UserService.class)
@TableView(defaultColumns = { "username", "name"}, requiresUpdate = true,
			actions = { @TableAction(bundle = "default", icon = "fa-key", 
	resourceKey = "setPassword", target = Target.ROW, url = "/app/ui/set-password/{uuid}")})
public abstract class User extends UUIDEntity implements NamedDocument {

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
	
	@ObjectField(required = false,
			searchable = true,
			nameField = false,
			type = FieldType.TEXT)
	String email;
	
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}

package com.jadaptive.api.repository;

import java.util.Collection;
import java.util.HashSet;

import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;

@ObjectViews({ 
	@ObjectViewDefinition(value = AssignableUUIDEntity.USERS_VIEW, bundle = "users"),
	@ObjectViewDefinition(value = AssignableUUIDEntity.ROLES_VIEW, bundle = "roles", weight = 50)})
public abstract class AssignableUUIDEntity extends AbstractUUIDEntity {

	private static final long serialVersionUID = -5734236381558890213L;
	
	public static final String USERS_VIEW = "users";
	public static final String ROLES_VIEW = "roles";
	
	@ObjectField(defaultValue = "false", 
			type = FieldType.OBJECT_REFERENCE,
			searchable = true,
			references = "roles")
	@ObjectView(ROLES_VIEW)
	@ExcludeView(values = FieldView.TABLE)
	Collection<String> roles = new HashSet<>();
	
	@ObjectField(defaultValue = "false", 
			type = FieldType.OBJECT_REFERENCE,
			searchable = true,
			references = "users")
	@ObjectView(USERS_VIEW)
	@ExcludeView(values = FieldView.TABLE)
	Collection<String> users = new HashSet<>();
	
	public Collection<String> getRoles() {
		return roles;
	}
	
	public void setRoles(Collection<String> roles) {
		this.roles = roles;
	}
	
	public Collection<String> getUsers() {
		return users;
	}
	
	public void setUsers(Collection<String> users) {
		this.users = users;
	}
	
	
}

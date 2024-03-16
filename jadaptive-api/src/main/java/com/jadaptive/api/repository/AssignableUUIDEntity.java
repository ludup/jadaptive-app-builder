package com.jadaptive.api.repository;

import java.util.Collection;
import java.util.HashSet;

import com.jadaptive.api.role.Role;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.user.User;

@ObjectViews({ 
	@ObjectViewDefinition(value = AssignableUUIDEntity.USERS_VIEW, bundle = "users", weight = Integer.MAX_VALUE-1),
	@ObjectViewDefinition(value = AssignableUUIDEntity.ROLES_VIEW, bundle = "roles", weight = Integer.MAX_VALUE)})
public abstract class AssignableUUIDEntity extends AbstractUUIDEntity implements AssignableDocument {

	private static final long serialVersionUID = -5734236381558890213L;
	
	public static final String USERS_VIEW = "userTab";
	public static final String ROLES_VIEW = "roleTab";
	
	@ObjectField(
			type = FieldType.OBJECT_REFERENCE,
			references = "roles")
	@ObjectView(ROLES_VIEW)
	@ExcludeView(values = FieldView.TABLE)
	Collection<Role> roles = new HashSet<>();
	
	@ObjectField(
			type = FieldType.OBJECT_REFERENCE,
			references = "users")
	@ObjectView(USERS_VIEW)
	@ExcludeView(values = FieldView.TABLE)
	Collection<User> users = new HashSet<>();
	
	public Collection<Role> getRoles() {
		return roles;
	}
	
	public void setRoles(Collection<Role> roles) {
		this.roles = roles;
	}
	
	public Collection<User> getUsers() {
		return users;
	}
	
	public void setUsers(Collection<User> users) {
		this.users = users;
	}
	
	
}

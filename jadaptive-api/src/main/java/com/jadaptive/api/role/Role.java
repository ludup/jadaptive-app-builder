package com.jadaptive.api.role;

import java.util.Collection;
import java.util.HashSet;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ExcludeView;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.FieldView;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.template.ObjectViews;
import com.jadaptive.api.template.TableView;

@ObjectDefinition(resourceKey = Role.RESOURCE_KEY, 
					scope = ObjectScope.GLOBAL,
					type = ObjectType.COLLECTION,
					defaultColumn = "name")
@ObjectViews({ 
	@ObjectViewDefinition(value = Role.USERS_VIEW, bundle = "users"),
	@ObjectViewDefinition(value = Role.PERMISSIONS_VIEW, bundle = "permissions", weight = 50),
	@ObjectViewDefinition(value = Role.OPTIONS_VIEW, bundle = Role.RESOURCE_KEY, weight = 100)})
@TableView(defaultColumns = { "name", "allPermissions", "allUsers" })
@GenerateEventTemplates(Role.RESOURCE_KEY)
public class Role extends NamedUUIDEntity {
	
	private static final long serialVersionUID = -5211370653998523985L;

	public static final String RESOURCE_KEY = "roles";
	
	public static final String USERS_VIEW = "users";
	public static final String PERMISSIONS_VIEW = "permissions";
	public static final String OPTIONS_VIEW = "options";
	
	@ObjectField(defaultValue = "false", 
			type = FieldType.BOOL)
	@ObjectView(OPTIONS_VIEW)
	boolean allPermissions;
	
	@ObjectField(defaultValue = "false", 
			type = FieldType.BOOL)
	@ObjectView(OPTIONS_VIEW)
	boolean allUsers;
	
	@ObjectField(
			type = FieldType.PERMISSION,
			searchable = true)
	@ObjectView(PERMISSIONS_VIEW)
	@ExcludeView(values = FieldView.TABLE)
	Collection<String> permissions = new HashSet<>();
	
	@ObjectField(
			type = FieldType.OBJECT_REFERENCE,
			searchable = true,
			references = "users")
	@ObjectView(USERS_VIEW)
	@ExcludeView(values = FieldView.TABLE)
	Collection<String> users = new HashSet<>();
	
	public String getResourceKey() {
		return RESOURCE_KEY;
	}
	
	public Collection<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Collection<String> permissions) {
		this.permissions = permissions;
	}

	public boolean isAllPermissions() {
		return allPermissions;
	}

	public void setAllPermissions(boolean allPermissions) {
		this.allPermissions = allPermissions;
	}

	public boolean isAllUsers() {
		return allUsers;
	}

	public void setAllUsers(boolean allUsers) {
		this.allUsers = allUsers;
	}

	public Collection<String> getUsers() {
		return users;
	}

	public void setUsers(Collection<String> users) {
		this.users = users;
	}
	
}

package com.jadaptive.api.role;

import java.util.Collection;
import java.util.HashSet;

import com.jadaptive.api.entity.ObjectScope;
import com.jadaptive.api.entity.ObjectType;
import com.jadaptive.api.repository.NamedUUIDEntity;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;

@ObjectDefinition(resourceKey = Role.RESOURCE_KEY, scope = ObjectScope.GLOBAL, type = ObjectType.COLLECTION)
public class Role extends NamedUUIDEntity {
	
	private static final long serialVersionUID = -5211370653998523985L;

	public static final String RESOURCE_KEY = "roles";
	
	@ObjectField(name = "All Permissions", 
			description = "Flag to indicate that this role contains all available permissions",
			defaultValue = "false", 
			type = FieldType.BOOL)
	boolean allPermissions;
	
	@ObjectField(name = "All Users", 
			description = "Flag to indicate that this role contains all available users",
			defaultValue = "false", 
			type = FieldType.BOOL)
	boolean allUsers;
	
	@ObjectField(name = "Permissions", 
			description = "The permissions assigned to this Role",
			defaultValue = "false", 
			type = FieldType.TEXT,
			searchable = true)
	Collection<String> permissions = new HashSet<>();
	
	@ObjectField(name = "Users", 
			description = "The users assigned to this Role",
			defaultValue = "false", 
			type = FieldType.OBJECT_REFERENCE,
			searchable = true,
			references = "users")
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

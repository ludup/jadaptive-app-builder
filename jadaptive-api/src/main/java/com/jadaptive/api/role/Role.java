package com.jadaptive.api.role;

import java.util.Collection;
import java.util.HashSet;

import com.jadaptive.app.repository.NamedUUIDEntity;

public class Role extends NamedUUIDEntity {

	
	boolean allPermissions;
	boolean allUsers;
	Collection<String> permissions = new HashSet<>();
	Collection<String> users = new HashSet<>();
	
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

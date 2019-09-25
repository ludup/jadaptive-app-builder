package com.jadaptive.role;

import java.util.Collection;

import com.jadaptive.repository.NamedUUIDEntity;

public class Role extends NamedUUIDEntity {

	Collection<String> permissions;

	public Collection<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Collection<String> permissions) {
		this.permissions = permissions;
	}
	
	
}

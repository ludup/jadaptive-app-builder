package com.jadaptive.api.repository;

import java.util.Collection;

import com.jadaptive.api.role.Role;
import com.jadaptive.api.user.User;

public interface AssignableDocument extends UUIDDocument {

	public Collection<User> getUsers();
	
	public Collection<Role> getRoles();
}

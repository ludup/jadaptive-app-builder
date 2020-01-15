package com.jadaptive.app.tenant;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleRepository;
import com.jadaptive.api.user.User;
import com.jadaptive.app.db.DocumentDatabase;
import com.jadaptive.utils.EntityUtils;

@Repository
public class AssignableObjectDatabaseImpl<T extends AssignableUUIDEntity> 
		extends TenantAwareObjectDatabaseImpl<T> implements AssignableObjectDatabase<T> {
	
	@Autowired
	RoleRepository roleRepository; 
	
	protected AssignableObjectDatabaseImpl(DocumentDatabase db) {
		super(db);
	}

	protected void assign(T e, Collection<Role> roles, Collection<User> users) {
		
		for(Role role : roles) {
			e.getRoles().add(role.getUuid());
		}
		
		for(User user : users) {
			e.getUsers().add(user.getUuid());
		}
		
		saveOrUpdate(e);
	}
	
	@Override
	public Collection<T> getAssignedObjects(Class<T> resourceClass, User user) {
		
		Collection<Role> userRoles = roleRepository.getRolesByUser(user);
		return searchObjects(resourceClass, 
				SearchField.in("users", user.getUuid()),
				SearchField.in("roles", EntityUtils.getUUIDs(userRoles)));
	}
	
}

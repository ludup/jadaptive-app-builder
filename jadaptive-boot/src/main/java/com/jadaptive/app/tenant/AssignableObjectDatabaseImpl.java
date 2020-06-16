package com.jadaptive.app.tenant;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.UUIDObjectUtils;

@Repository
public class AssignableObjectDatabaseImpl<T extends AssignableUUIDEntity> implements AssignableObjectDatabase<T> {
	
	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private TenantAwareObjectDatabase<T> objectDatabase;
	
	protected void assign(T e, Collection<Role> roles, Collection<User> users) {
		
		for(Role role : roles) {
			e.getRoles().add(role.getUuid());
		}
		
		for(User user : users) {
			e.getUsers().add(user.getUuid());
		}
		
		objectDatabase.saveOrUpdate(e);
	}
	
	@Override
	public Collection<T> getAssignedObjects(Class<T> resourceClass, User user) {
		
		Collection<Role> userRoles = roleService.getRolesByUser(user);
		return objectDatabase.searchObjects(resourceClass, 
				SearchField.or(
						SearchField.in("users", user.getUuid()),
						SearchField.in("roles", UUIDObjectUtils.getUUIDs(userRoles))
				));
	}

	@Override
	public T getObjectByUUID(Class<T> resourceClass, String uuid) {
		return objectDatabase.get(uuid, resourceClass);
	}

	@Override
	public void saveOrUpdate(T obj) {
		objectDatabase.saveOrUpdate(obj);
	}

	@Override
	public void deleteObject(T obj) {
		objectDatabase.delete(obj);
	}

	@Override
	public T getObject(Class<T> resourceClass, User user, SearchField... fields) {
		
		Collection<Role> userRoles = roleService.getRolesByUser(user);
		
		if(fields.length > 0) {
			return objectDatabase.get(resourceClass, SearchField.and(SearchField.and(fields), 
					SearchField.or(SearchField.in("users", user.getUuid()),
									SearchField.in("roles", UUIDObjectUtils.getUUIDs(userRoles)))));
		} else {
			return objectDatabase.get(resourceClass, SearchField.or(
				SearchField.in("users", user.getUuid()),
				SearchField.in("roles", UUIDObjectUtils.getUUIDs(userRoles))));
		}
	}

	@Override
	public Iterable<T> getObjects(Class<T> resourceClass) {
		return objectDatabase.list(resourceClass);
	}

	
}

package com.jadaptive.app.tenant;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.jadaptive.api.db.AssignableObjectDatabase;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.TenantAwareObjectDatabase;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.repository.AssignableDocument;
import com.jadaptive.api.role.Role;
import com.jadaptive.api.role.RoleService;
import com.jadaptive.api.template.SortOrder;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.UUIDObjectUtils;

@Repository
public class AssignableObjectDatabaseImpl<T extends AssignableDocument> implements AssignableObjectDatabase<T> {
	
	@Autowired
	private RoleService roleService; 
	
	@Autowired
	private TenantAwareObjectDatabase<T> objectDatabase;
	
	@Autowired
	private PermissionService permissionService; 
	
	protected void assign(T e, Collection<Role> roles, Collection<User> users) {
		
		for(Role role : roles) {
			e.getRoles().add(role);
		}
		
		for(User user : users) {
			e.getUsers().add(user);
		}
		
		objectDatabase.saveOrUpdate(e);
	}
	
	@Override
	public Iterable<T> getAssignedObjects(Class<T> resourceClass, User user, SortOrder order, String sortField, SearchField... fields) {
		
		if(permissionService.isAdministrator(user)) {
			 return getObjects(resourceClass);
		} else {
			Collection<Role> userRoles = roleService.getRolesByUser(user);
			return objectDatabase.searchObjects(resourceClass, 
					order,
					sortField,
					SearchField.and(fields),
					SearchField.or(
							SearchField.all("users.uuid", user.getUuid()),
							SearchField.in("roles.uuid", UUIDObjectUtils.getUUIDs(userRoles))
					));
		}
	}
	
	@Override
	public Iterable<T> getAssignedObjects(Class<T> resourceClass, User user, SearchField... fields) {
		
		if(permissionService.isAdministrator(user)) {
			 return getObjects(resourceClass);
		} else {
			Collection<Role> userRoles = roleService.getRolesByUser(user);
			return objectDatabase.searchObjects(resourceClass, 
					SearchField.add(fields,
					SearchField.or(
							SearchField.all("users.uuid", user.getUuid()),
							SearchField.in("roles.uuid", UUIDObjectUtils.getUUIDs(userRoles)))
					));
		}
	}
	
	@Override
	public Iterable<T> getAssignedObjectsA(Class<T> resourceClass, User user, SearchField... fields) {
		
		Collection<Role> userRoles = roleService.getRolesByUser(user);
		return objectDatabase.searchObjects(resourceClass, 
				SearchField.add(fields,
				SearchField.or(
						SearchField.all("users.uuid", user.getUuid()),
						SearchField.in("roles.uuid", UUIDObjectUtils.getUUIDs(userRoles)))
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
	public T getAssignedObject(Class<T> resourceClass, User user, SearchField... fields) {
		
		if(permissionService.isAdministrator(user)) {
			 return getObject(resourceClass, fields);
		} else {
			Collection<Role> userRoles = roleService.getRolesByUser(user);
			
			if(fields.length > 0) {
				return objectDatabase.get(resourceClass, SearchField.and(SearchField.and(fields), 
						SearchField.or(SearchField.all("users.uuid", user.getUuid()),
										SearchField.in("roles.uuid", UUIDObjectUtils.getUUIDs(userRoles)))));
			} else {
				return objectDatabase.get(resourceClass, SearchField.or(
					SearchField.all("users.uuid", user.getUuid()),
					SearchField.in("roles.uuid", UUIDObjectUtils.getUUIDs(userRoles))));
			}
		}
	}

	@Override
	public Iterable<T> getObjects(Class<T> resourceClass) {
		return objectDatabase.list(resourceClass);
	}

	@Override
	public T getObject(Class<T> resourceClass, SearchField... fields) {
		return objectDatabase.get(resourceClass, fields);
	}

	@Override
	public long countObjects(Class<T> resourceClass, SearchField... fields) {
		return objectDatabase.count(resourceClass, fields);
	}

	@Override
	public Collection<T> searchObjects(Class<T> clz, SearchField... fields) {
		return objectDatabase.searchObjects(clz, fields);
	}
	
}

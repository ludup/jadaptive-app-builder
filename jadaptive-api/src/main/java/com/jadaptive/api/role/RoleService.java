package com.jadaptive.api.role;

import java.util.Arrays;
import java.util.Collection;

import com.jadaptive.api.permissions.PermissionUtils;
import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.user.User;
import com.jadaptive.utils.ObjectUtils;

public interface RoleService {

	public static final String READ_PERMISSION = PermissionUtils.getReadPermission(Role.RESOURCE_KEY);
	public static final String READ_WRITE_PERMISSION = PermissionUtils.getReadWritePermission(Role.RESOURCE_KEY);
			
	public static final String ADMINISTRATOR_UUID = "1bfbaf16-e5af-4825-8f8a-83ce2f5bf81f";
	public static final String EVERYONE_UUID = "c4b54f49-c478-46cc-8cfa-aaebaa4ea50f";
	public static final String EVERYONE = "Everyone";
	public static final String ADMINISTRATION = "Administration";
	
	
	Role getAdministrationRole();

	Role getEveryoneRole();

	void assignRole(Role role, User... user);

	void unassignRole(Role role, User... user);

	Collection<Role> getRoles(User user);

	Role getRoleByName(String name);

	Role createRole(String roleName, User... users);
	
	Role createRole(String roleName, Collection<User> users);

	void grantPermission(Role role, String... permissions);

	void revokePermission(Role role, String... permissions);

	boolean hasRole(User currentUser, Collection<Role> roles);

	boolean hasRole(User currentUser, Role... roles);

	void deleteRole(Role role);

	Iterable<Role> listRoles();

	Role getRoleByUUID(String roleUUID);

	Iterable<Role> allRoles();

	Collection<Role> getRolesByUser(User user);

	Collection<Role> getAllUserRoles();
	
	boolean isAssigned(AssignableUUIDEntity obj, User user);

	Collection<Role> getRolesByUUID(Collection<String> roles);

	Collection<String> getUsersByRoles(Collection<String> roles);
	
	default Collection<String> getUsersByRoles(Role... roles) { return ObjectUtils.entityToUUIDCollection(Arrays.asList(roles)); };

	default Collection<String> getUsersByRoles(String... roles) { return getUsersByRoles(Arrays.asList(roles)); };
	
	void compareAssignments(AssignableUUIDEntity current, 
			AssignableUUIDEntity previous, 
			Collection<String> assignments,
			Collection<String> unassignments);

	Collection<Role> getAdministrationRoles();

}

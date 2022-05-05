package com.jadaptive.api.permissions;

import java.util.Collection;
import java.util.Set;

import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.ui.NamePairValue;
import com.jadaptive.api.user.User;

public interface PermissionService {

	public static final String READ = "read";
	public static final String READ_WRITE = "readWrite";
	
	void assertAnyPermission(String... permissions) throws AccessDeniedException;

	void assertPermission(String permission) throws AccessDeniedException;

	void assertRead(String resourceKey) throws AccessDeniedException;

	void assertReadWrite(String resourceKey) throws AccessDeniedException;

	void registerStandardPermissions(String resourceKey);

	Collection<String> getAllPermissions();
	
	Collection<NamePairValue> getPermissions();

	Collection<String> getAllPermissions(Tenant tenant);

	void setupUserContext(User user);

	User getCurrentUser();

	void setupSystemContext();

	void clearUserContext();

	void registerCustomPermission(String customPermission);

	boolean isValidPermission(String permission);

	void assertAllPermission(String... permissions);

	boolean hasUserContext();

	Set<String> resolvePermissions(User user);

	Set<String> resolveCurrentPermissions();

	boolean isAdministrator(User user);

	void assertAdministrator();

}

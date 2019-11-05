package com.jadaptive.permissions;

import java.util.Collection;

import com.jadaptive.tenant.Tenant;
import com.jadaptive.user.User;

public interface PermissionService {

	void assertAnyPermission(String... permissions) throws PermissionDeniedException;

	void assertPermission(String permission) throws PermissionDeniedException;

	void assertRead(String resourceKey) throws PermissionDeniedException;

	void assertReadWrite(String resourceKey) throws PermissionDeniedException;

	void registerStandardPermissions(String resourceKey);

	Collection<String> getAllPermissions();

	Collection<String> getAllPermissions(Tenant tenant);

	void setupUserContext(User user);

	User getCurrentUser();

	void setupSystemContext();

	void clearUserContext();

	void registerCustomPermission(String customPermission);

	boolean isValidPermission(String permission);

}

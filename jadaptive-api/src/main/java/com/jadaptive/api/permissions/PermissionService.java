package com.jadaptive.api.permissions;

import java.util.Collection;

import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.user.User;

public interface PermissionService {

	void assertAnyPermission(String... permissions) throws AccessDeniedException;

	void assertPermission(String permission) throws AccessDeniedException;

	void assertRead(String resourceKey) throws AccessDeniedException;

	void assertReadWrite(String resourceKey) throws AccessDeniedException;

	void registerStandardPermissions(String resourceKey);

	Collection<String> getAllPermissions();

	Collection<String> getAllPermissions(Tenant tenant);

	void setupUserContext(User user);

	User getCurrentUser();

	void setupSystemContext();

	void clearUserContext();

	void registerCustomPermission(String customPermission);

	boolean isValidPermission(String permission);

	void assertAllPermission(String... permissions);

	boolean hasUserContext();

}

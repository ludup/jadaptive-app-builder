package com.jadaptive.permissions;

import java.util.Collection;

public interface PermissionService {

	void assertAnyPermission(String resourceKey, String... permissions) throws PermissionDeniedException;

	void assertPermission(String resourceKey, String permission) throws PermissionDeniedException;

	void assertRead(String resourceKey) throws PermissionDeniedException;

	void assertReadWrite(String resourceKey) throws PermissionDeniedException;

	void registerStandardPermissions(String resourceKey);

	void registerCustomPermission(String resourceKey, String permission);

	Collection<String> getAllPermissions();

}

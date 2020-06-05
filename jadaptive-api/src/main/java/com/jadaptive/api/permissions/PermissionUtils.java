package com.jadaptive.api.permissions;

public class PermissionUtils {


	public static String getReadWritePermission(String resourceKey) {
		return String.format("%s.%s", resourceKey, PermissionService.READ_WRITE);
	}

	public static String getReadPermission(String resourceKey) {
		return String.format("%s.%s", resourceKey, PermissionService.READ);
	}
}

package com.jadaptive.api.permissions;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

import com.jadaptive.api.repository.AssignableUUIDEntity;
import com.jadaptive.api.repository.UUIDDocument;
import com.jadaptive.api.session.UnauthorizedException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.ui.NamePairValue;
import com.jadaptive.api.user.User;

public interface PermissionService {

	public static final String READ = "read";
	public static final String READ_WRITE = "readWrite";
	public static final Set<String> NO_PERMISSIONS = Collections.emptySet();
	
	void assertAnyPermission(String... permissions) throws AccessDeniedException;

	void assertPermission(String permission) throws AccessDeniedException;

	void assertRead(String resourceKey) throws AccessDeniedException;
	
//	void assertRead(ObjectTemplate resourceKey) throws AccessDeniedException;

	void registerStandardPermissions(String resourceKey);

	Collection<String> getAllPermissions();
	
	Collection<NamePairValue> getPermissions();

	Collection<String> getAllPermissions(Tenant tenant);

	void setupUserContext(User user);

	User getCurrentUser() throws UnauthorizedException;

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

	void assertAssignment(AssignableUUIDEntity obj);
	
	

	<T> T as(User user, Callable<T> call);

	<T> T asSystem(Callable<T> call);

//	void assertOwnership(PersonalUUIDEntity obj);
//
//	void assertOwnership(AbstractObject e);

//	void assertWrite(ObjectTemplate template);
	
	void assertWrite(String resourceKey);

	default boolean isAdministrator() { return isAdministrator(getCurrentUser()); }

	void assertOwnership(UUIDDocument e);

	User getSystemUser();

	UncheckedCloseable systemContext();

	UncheckedCloseable userContext();

	UncheckedCloseable userContext(User user);
	
	@FunctionalInterface
	public interface UncheckedCloseable extends Closeable {
		@Override
		void close();
	}

	void assertAnyResolvedPermission(Set<String> resolvedPermissions, String... permissions)
			throws AccessDeniedException;

}

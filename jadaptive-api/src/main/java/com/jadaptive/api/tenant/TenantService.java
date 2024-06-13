package com.jadaptive.api.tenant;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import com.jadaptive.api.entity.ObjectException;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService.UncheckedCloseable;
import com.jadaptive.api.repository.RepositoryException;
import com.jadaptive.api.repository.UUIDObjectService;

public interface TenantService extends UUIDObjectService<Tenant> {

	public static final String SYSTEM_UUID = "4f1b781c-581d-474f-9505-4fea9c5e3909";
	
	Tenant getCurrentTenant() throws RepositoryException, ObjectException;

	void setCurrentTenant(Tenant tenant);

	default void runAsTenant(Tenant tenant, Runnable task) {
		var was = getCurrentTenant();
		try {
			setCurrentTenant(tenant);
			task.run();
		}
		finally {
			setCurrentTenant(was);
		}
	}
	
	default UncheckedCloseable tenant(Tenant tenant) {
		var was = getCurrentTenant();
		setCurrentTenant(tenant);
		return () -> setCurrentTenant(was);
	}

	void clearCurrentTenant();

	Iterable<Tenant> allObjects();
	
	default UncheckedCloseable systemTenant() {
		return tenant(getSystemTenant());
	}

	Tenant getSystemTenant() throws RepositoryException, ObjectException;

	void setCurrentTenant(HttpServletRequest request);

	void setCurrentTenant(String name);

	void deleteTenant(Tenant tenant);

	Tenant getTenantByDomainOrDefault(String name);

	Tenant getTenantByDomain(String name);

	void assertManageTenant() throws AccessDeniedException;

	Tenant resolveTenantName(String username);

	Tenant getTenantByName(String name);

	Tenant getTenantByUUID(String uuid);

	boolean isSetupMode();

	void completeSetup();

	boolean isReady();

	void executeAs(Tenant tenant, Runnable r);
	
	<T> T executeAs(Tenant tenant, Callable<T> r);

	boolean hasCurrentTenant();

	Tenant initialiseTenant(Tenant tenant, boolean newSchema);

	Tenant createTenant(String name, String ownerName, String ownerEmail, String primaryDomain,
			boolean system, String... additionalDomains) throws RepositoryException, ObjectException;

	Tenant createTenant(String uuid, String name, String ownerName, String ownerEmail, String primaryDomain,
			boolean system, String... additionalDomains) throws RepositoryException, ObjectException;

	Tenant createTenant(String uuid, String name, String primaryDomain, boolean system);

	boolean isSystemTenant();

	<T> T asSystem(Callable<T> r);

	void asSystem(Runnable r);

	String resolveUserName(String username);

	void execute(Runnable r);

	<T> T execute(Callable<T> r);

	void setSystemOwner(String company, String name, String emailAddress);

	void onSetupComplete(Runnable run);
}

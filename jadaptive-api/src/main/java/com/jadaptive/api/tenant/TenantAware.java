package com.jadaptive.api.tenant;

import com.jadaptive.api.app.App;

public interface TenantAware {

	public default void initializeSystem(boolean newSchema) { 
		initializeTenant(App.bean(TenantService.class).getSystemTenant(), newSchema);
	};
	
	public default void initializeTenant(Tenant tenant, boolean newSchema) { };
	
	public default void deleteTenant(Tenant tenant) { };
	
	public default void deleteScheduled(Tenant tenant) { };
	
	public default void deleteCancelled(Tenant tenant) { };

	public default Integer getOrder() { return Integer.MAX_VALUE; };
}

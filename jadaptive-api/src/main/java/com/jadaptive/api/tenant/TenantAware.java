package com.jadaptive.api.tenant;

public interface TenantAware {

	public default void initializeSystem(boolean newSchema) { };
	
	public default void initializeTenant(Tenant tenant, boolean newSchema) { };

	public default Integer getOrder() { return Integer.MAX_VALUE; };
}

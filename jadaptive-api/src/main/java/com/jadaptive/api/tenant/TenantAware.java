package com.jadaptive.api.tenant;

public interface TenantAware {

	public void initializeSystem(boolean newSchema);
	
	public void initializeTenant(Tenant tenant, boolean newSchema);
}

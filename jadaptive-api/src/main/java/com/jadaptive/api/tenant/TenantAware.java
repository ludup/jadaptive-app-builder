package com.jadaptive.api.tenant;

public interface TenantAware {

	public void initializeSystem();
	
	public void initializeTenant(Tenant tenant);
}

package com.jadaptive.tenant;

public interface TenantService {

	Tenant getCurrentTenant();

	Tenant getDefaultTenant();

	void setCurrentTenant(Tenant tenant);

	void clearCurrentTenant();

}

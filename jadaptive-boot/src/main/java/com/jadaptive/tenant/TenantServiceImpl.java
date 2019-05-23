package com.jadaptive.tenant;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class TenantServiceImpl implements TenantService {

	ThreadLocal<Tenant> currentTenant = new ThreadLocal<>();
	final Tenant DEFAULT_LOCALHOST = new Tenant("cb3129ea-b8b1-48a4-85de-8443945d95e3", "System", "localhost");
	
	@Override
	public Tenant getCurrentTenant() {
		Tenant tenant = currentTenant.get();
		if(Objects.isNull(tenant)) {
			return DEFAULT_LOCALHOST;
		}
		return tenant;
	}

	@Override
	public Tenant getDefaultTenant() {
		return DEFAULT_LOCALHOST;
	}
	
	@Override
	public void setCurrentTenant(Tenant tenant) {
		currentTenant.set(tenant);
	}
	
	@Override
	public void clearCurrentTenant() {
		currentTenant.remove();
	}
}

package com.jadaptive.repository;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.templates.TemplateEnabledUUIDRepositoryImpl;
import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantService;

public abstract class TenantAwareUUIDRepositoryImpl<E extends AbstractUUIDEntity> extends TemplateEnabledUUIDRepositoryImpl<E> {

	@Autowired
	TenantService tenantService;

	protected String getTenantUuid() {
		Tenant tenant;
		try {
			tenant = tenantService.getCurrentTenant();
			if(Objects.isNull(tenant)) {
				throw new IllegalStateException("Tenant required but no tenant is available in the current thread context");
			}
			return tenant.getUuid();
		} catch (RepositoryException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
	}
}

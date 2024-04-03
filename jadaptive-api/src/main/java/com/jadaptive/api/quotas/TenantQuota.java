package com.jadaptive.api.quotas;

import java.util.Collection;

import com.jadaptive.api.events.GenerateEventTemplates;
import com.jadaptive.api.template.FieldType;
import com.jadaptive.api.template.ObjectDefinition;
import com.jadaptive.api.template.ObjectField;
import com.jadaptive.api.template.ObjectView;
import com.jadaptive.api.template.ObjectViewDefinition;
import com.jadaptive.api.tenant.Tenant;

@ObjectDefinition(resourceKey = TenantQuota.RESOURCE_KEY, system = true, bundle = QuotaThreshold.RESOURCE_KEY)
@ObjectViewDefinition(value = TenantQuota.TENANTS_VIEW, weight = 99999)
@GenerateEventTemplates
public class TenantQuota extends QuotaThreshold {

	private static final long serialVersionUID = 6344677508192690706L;
	public static final String RESOURCE_KEY = "tenantQuotas";
	public static final String TENANTS_VIEW = "tenantsQuotaView";
	
	@ObjectField(type = FieldType.BOOL, defaultValue = "false")
	@ObjectView(TENANTS_VIEW)
	Boolean allTenants;
	
	@ObjectField(type = FieldType.OBJECT_REFERENCE, references = Tenant.RESOURCE_KEY)
	@ObjectView(value = TENANTS_VIEW, dependsOn = "allTenants", dependsValue = "false")
	Collection<Tenant> tenants;

	public Collection<Tenant> getTenants() {
		return tenants;
	}

	public void setTenants(Collection<Tenant> tenants) {
		this.tenants = tenants;
	}
	
	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	public Boolean getAllTenants() {
		return allTenants;
	}

	public void setAllTenants(Boolean allTenants) {
		this.allTenants = allTenants;
	}
	
}

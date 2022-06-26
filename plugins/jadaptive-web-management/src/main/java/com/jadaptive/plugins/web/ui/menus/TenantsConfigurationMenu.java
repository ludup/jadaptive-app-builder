package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.config.ConfigurationPageItem;
import com.jadaptive.api.tenant.TenantService;

@Extension
public class TenantsConfigurationMenu implements ConfigurationPageItem {

	@Autowired
	private TenantService tenantService; 
	
	@Override
	public String getResourceKey() {
		return "tenantConfiguration";
	}

	@Override
	public String getBundle() {
		return "tenantConfiguration";
	}

	@Override
	public String getPath() {
		return "/app/ui/system/tenantConfiguration";
	}

	@Override
	public String getIcon() {
		return "house-flag";
	}

	@Override
	public Integer weight() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

	@Override
	public boolean isSystem() {
		return true;
	}

	@Override
	public boolean isVisible() {
		return tenantService.supportsMultipleTenancy();
	}
}

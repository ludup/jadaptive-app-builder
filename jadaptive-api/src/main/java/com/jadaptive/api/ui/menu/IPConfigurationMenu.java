package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.jadaptive.api.config.ConfigurationPageItem;
import com.jadaptive.api.ip.IPStackConfiguration;
import com.jadaptive.api.permissions.PermissionUtils;

@Component
public class IPConfigurationMenu implements ConfigurationPageItem {

	@Override
	public String getResourceKey() {
		return IPStackConfiguration.RESOURCE_KEY;
	}

	@Override
	public boolean isSystem() {
		return true;
	}

	@Override
	public String getBundle() {
		return IPStackConfiguration.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/system/" + IPStackConfiguration.RESOURCE_KEY;
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList(PermissionUtils.getReadPermission(IPStackConfiguration.RESOURCE_KEY));
	}

	@Override
	public String getIcon() {
		return "fa-location-dot";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

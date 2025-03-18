package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.jadaptive.api.app.App;
import com.jadaptive.api.tenant.FeatureEnablementService;
import com.jadaptive.api.ui.pages.config.ConfigurationPage;

@Component
public class SystemConfigurationMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "configuration.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/options";
	}
	
	@Override
	public boolean isEnabled() {
		return App.bean(FeatureEnablementService.class).isEnabled(ConfigurationPage.CONFIGURATION_FEATURE);
	}

	@Override
	public String getIcon() {
		return "fa-gears";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.CONFIGURATION_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "ed3bad60-c61f-4a65-9e2a-12e590f2352a";
	}

	@Override
	public Integer weight() {
		return 0;
	}
	
	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

}

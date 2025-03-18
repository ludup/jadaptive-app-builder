package com.jadaptive.app.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.jadaptive.api.app.App;
import com.jadaptive.api.tenant.FeatureEnablementService;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.api.ui.pages.config.ConfigurationPage;

@Component
public class ConfigurationMenu implements ApplicationMenu {

	@Override
	public String getUuid() {
		return ApplicationMenuService.CONFIGURATION_MENU_UUID;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public String getI18n() {
		return "administration.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

	@Override
	public String getIcon() {
		return "fa-wrench";
	}

	@Override
	public String getParent() {
		return null;
	}
	
	@Override
	public Integer weight() {
		return Integer.MAX_VALUE;
	}

}

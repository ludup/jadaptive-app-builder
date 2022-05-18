package com.jadaptive.plugins.web.ui.menus;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class ConfigurationMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "configuration.name";
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
	public String getIcon() {
		return "gear";
	}

	@Override
	public String getParent() {
		return null;
	}

	@Override
	public String getUuid() {
		return ApplicationMenuService.CONFIGURATION_MENU;
	}

	@Override
	public Integer weight() {
		return 500;
	}

}

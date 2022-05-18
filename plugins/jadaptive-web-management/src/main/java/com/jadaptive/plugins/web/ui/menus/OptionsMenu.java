package com.jadaptive.plugins.web.ui.menus;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class OptionsMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "options.name";
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
	public String getIcon() {
		return "gears";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.CONFIGURATION_MENU;
	}

	@Override
	public String getUuid() {
		return "3ef00f24-cfd1-4730-b357-0d5f79828bd2";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

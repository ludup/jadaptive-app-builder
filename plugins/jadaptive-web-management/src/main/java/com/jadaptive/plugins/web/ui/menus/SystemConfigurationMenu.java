package com.jadaptive.plugins.web.ui.menus;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class SystemConfigurationMenu implements ApplicationMenu {

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
		return "/app/ui/systemConfiguration";
	}

	@Override
	public String getIcon() {
		return "gears";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "ed3bad60-c61f-4a65-9e2a-12e590f2352a";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

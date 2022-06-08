package com.jadaptive.plugins.web.ui.menus;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class AuthenticationModulesMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "authenticationModules.names";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/authentication-modules";
	}

	@Override
	public String getIcon() {
		return "id-card";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.ADMINISTRATION_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "71a9eef4-672c-4245-84e9-6cfe868bd985";
	}

	@Override
	public Integer weight() {
		return 999999;
	}

}

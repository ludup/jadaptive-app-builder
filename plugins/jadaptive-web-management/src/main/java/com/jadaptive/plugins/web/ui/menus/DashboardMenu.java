package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class DashboardMenu implements ApplicationMenu {

	
	@Override
	public String getResourceKey() {
		return "dashboard.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/dashboard";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

	@Override
	public String getIcon() {
		return "chart-line";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.HOME_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "89519436-6b47-44ff-8432-333de2d0a125";
	}

	@Override
	public Integer weight() {
		return 0;
	}
}

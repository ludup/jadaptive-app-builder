package com.jadaptive.plugins.dashboard.menus;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.plugins.dashboard.DashboardInitialiser;

@Component
public class DashboardMenu implements ApplicationMenu {

	
	public static final String MENU_UUID = "89519436-6b47-44ff-8432-333de2d0a125";

	@Override
	public String getI18n() {
		return "dashboard.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList(DashboardInitialiser.DASHBOARD_PERMISSION); 
	}

	@Override
	public String getIcon() {
		return "fa-chart-line";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.HOME_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return MENU_UUID;
	}

	@Override
	public Integer weight() {
		return 0;
	}
}

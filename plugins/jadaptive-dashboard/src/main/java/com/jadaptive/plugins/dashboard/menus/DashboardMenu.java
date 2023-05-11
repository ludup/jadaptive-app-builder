package com.jadaptive.plugins.dashboard.menus;

import java.util.Collection;
import java.util.Collections;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Component
public class DashboardMenu implements ApplicationMenu {

	
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
		return Collections.emptyList();
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
		return "89519436-6b47-44ff-8432-333de2d0a125";
	}

	@Override
	public Integer weight() {
		return 0;
	}
}

package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class RolesMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "roles.names";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/table/roles";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("roles.read");
	}

	@Override
	public String getIcon() {
		return "user-md";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.ADMINISTRATION_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "15790c08-f0d1-4ba7-9e7e-da94118b5c07";
	}

	@Override
	public Integer weight() {
		return 10;
	}

}

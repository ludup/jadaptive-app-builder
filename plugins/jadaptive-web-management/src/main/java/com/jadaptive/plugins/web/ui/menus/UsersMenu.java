package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class UsersMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "users.names";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/table/users";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("builtinUsers.read");
	}

	@Override
	public String getIcon() {
		return "users";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.ADMINISTRATION_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "c8d39ed7-9f9d-484b-8305-04d1743d47ab";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

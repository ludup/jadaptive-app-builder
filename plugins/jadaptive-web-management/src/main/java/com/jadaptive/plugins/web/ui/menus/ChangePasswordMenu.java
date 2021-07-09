package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class ChangePasswordMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "changePassword.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/change-password";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("users.changePassword");
	}

	@Override
	public String getIcon() {
		return "key";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.HOME_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "dc8e76be-6cac-4ba6-be46-b963f25e0953";
	}

	@Override
	public Integer weight() {
		return 100;
	}

}

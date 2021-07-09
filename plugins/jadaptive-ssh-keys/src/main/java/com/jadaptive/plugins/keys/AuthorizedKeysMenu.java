package com.jadaptive.plugins.keys;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class AuthorizedKeysMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "authorizedKeys.names";
	}

	@Override
	public String getBundle() {
		return AuthorizedKey.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/table/authorizedKeys";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("users.login");
	}

	@Override
	public String getIcon() {
		return "shield-alt";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.HOME_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "cb3df972-5248-4053-b004-d19301a59e02";
	}

	@Override
	public Integer weight() {
		return 1000;
	}

}

package com.jadaptive.plugins.keys;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class AuthorizedKeysMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "authorizedKeys.names";
	}

	@Override
	public String getBundle() {
		return AuthorizedKey.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/search/authorizedKeys";
	}
	
	@Override
	public String getIcon() {
		return "fa-shield-alt";
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

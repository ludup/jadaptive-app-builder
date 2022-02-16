package com.jadaptive.plugins.logonbox.ui;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;
import com.jadaptive.plugins.logonbox.LogonBoxConfiguration;

@Extension
public class AuthenticatorConfigurationMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return LogonBoxConfiguration.RESOURCE_KEY + ".name";
	}

	@Override
	public String getBundle() {
		return LogonBoxConfiguration.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/config/" + LogonBoxConfiguration.RESOURCE_KEY;
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList(LogonBoxConfiguration.RESOURCE_KEY + ".read");
	}

	@Override
	public String getIcon() {
		return "far fa-mobile-alt";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "12331712-fd15-45cf-bc39-3f51bd9da69a";
	}

	@Override
	public Integer weight() {
		return 99999;
	}

}

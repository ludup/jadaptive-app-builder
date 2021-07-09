package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.session.SessionConfiguration;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class SessionConfigurationMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "sessionConfiguration.name";
	}

	@Override
	public String getBundle() {
		return SessionConfiguration.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/config/sessionConfiguration";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("sessionConfiguration.read");
	}

	@Override
	public String getIcon() {
		return "hourglass-half";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "18100e3e-c06d-42d5-9542-ca30987595bc";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class TenantsMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "tenant.names";
	}

	@Override
	public String getBundle() {
		return "tenant";
	}

	@Override
	public String getPath() {
		return "/app/ui/search/tenant";
	}

	@Override
	public String getIcon() {
		return "fa-database";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "9e936c73-ec12-4f66-8e07-6c0c0889b7d4";
	}

	@Override
	public Integer weight() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

}

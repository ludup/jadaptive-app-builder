package com.jadaptive.app.ui.menu;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class ResourcesMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "resources.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "";
	}

	@Override
	public Collection<String> getPermissions() {
		return Collections.emptyList();
	}

	@Override
	public String getIcon() {
		return "fa-toolbox";
	}

	@Override
	public String getParent() {
		return null;
	}

	@Override
	public String getUuid() {
		return ApplicationMenuService.RESOURCE_MENU_UUID;
	}
	
	@Override
	public Integer weight() {
		return 100;
	}

}

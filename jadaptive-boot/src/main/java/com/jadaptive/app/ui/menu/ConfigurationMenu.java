package com.jadaptive.app.ui.menu;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Component
public class ConfigurationMenu implements ApplicationMenu {

	@Override
	public String getUuid() {
		return ApplicationMenuService.CONFIGURATION_MENU_UUID;
	}
	
	@Override
	public boolean isVisible() {
		return true;
	}
	
	@Override
	public String getI18n() {
		return "administration.name";
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
		return Arrays.asList("tenant.read");
	}

	@Override
	public String getIcon() {
		return "fa-wrench";
	}

	@Override
	public String getParent() {
		return null;
	}
	
	@Override
	public Integer weight() {
		return Integer.MAX_VALUE;
	}

}

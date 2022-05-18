package com.jadaptive.app.ui.menu;

import java.util.Collection;
import java.util.Collections;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class SystemMenu implements ApplicationMenu {

	@Autowired
	private TenantService tenantService;
	
	@Override
	public String getUuid() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}
	
	@Override
	public boolean isVisible() {
		return tenantService.getCurrentTenant().isSystem();
	}
	
	@Override
	public String getResourceKey() {
		return "system.name";
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
		return "wrench";
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

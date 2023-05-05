package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

@Component
public class SystemConfigurationMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "configuration.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/systemConfiguration";
	}

	@Override
	public String getIcon() {
		return "fa-gears";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "ed3bad60-c61f-4a65-9e2a-12e590f2352a";
	}

	@Override
	public Integer weight() {
		return 0;
	}
	
	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

}

package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

@Component
public class RolesMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "roles.names";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/search/roles";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("roles.read");
	}

	@Override
	public String getIcon() {
		return "fa-user-md";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SECURITY_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "15790c08-f0d1-4ba7-9e7e-da94118b5c07";
	}

	@Override
	public Integer weight() {
		return 10;
	}

}

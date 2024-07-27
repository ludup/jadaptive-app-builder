package com.jadaptive.api.ui.menu;

import org.springframework.stereotype.Component;

@Component
public class LogoffMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "logoff.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/logoff";
	}

	@Override
	public String getIcon() {
		return "fa-sign-out-alt";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.USER_MENU;
	}

	@Override
	public String getUuid() {
		return "60aa2937-2980-4e3b-9e1e-643adde08b8d";
	}

	@Override
	public Integer weight() {
		return Integer.MAX_VALUE;
	}

}

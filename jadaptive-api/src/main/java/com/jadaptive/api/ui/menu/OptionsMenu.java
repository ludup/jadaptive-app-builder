package com.jadaptive.api.ui.menu;

import org.springframework.stereotype.Component;

@Component
public class OptionsMenu implements ApplicationMenu {
	

	@Override
	public String getI18n() {
		return "options.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/options";
	}

	@Override
	public String getIcon() {
		return "fa-gears";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.ADMINISTRATION_MENU;
	}

	@Override
	public String getUuid() {
		return "3ef00f24-cfd1-4730-b357-0d5f79828bd2";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

@Component
public class ChangePasswordMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "changePassword.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/change-my-password";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("users.changePassword");
	}

	@Override
	public String getIcon() {
		return "fa-key";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.HOME_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "dc8e76be-6cac-4ba6-be46-b963f25e0953";
	}

	@Override
	public Integer weight() {
		return 100;
	}

}

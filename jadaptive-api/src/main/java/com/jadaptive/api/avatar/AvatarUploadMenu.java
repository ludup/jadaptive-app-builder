package com.jadaptive.api.avatar;

import org.springframework.stereotype.Component;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Component
public class AvatarUploadMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "uploadAvatar.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/avatar-upload";
	}

	@Override
	public String getIcon() {
		return "fa-camera";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.USER_MENU;
	}

	@Override
	public String getUuid() {
		return "55a1530e-2e05-4ee1-88f8-5fc98911daae";
	}

	@Override
	public Integer weight() {
		return 200;
	}

}

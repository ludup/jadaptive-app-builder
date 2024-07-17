package com.jadaptive.api.ui.menu;

import org.springframework.stereotype.Component;

import com.jadaptive.api.session.Session;

@Component
public class ImpersonatingMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return "stopImpersonating.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/revert-impersonation";
	}

	@Override
	public String getIcon() {
		return "fa-person";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.USER_MENU;
	}

	@Override
	public String getUuid() {
		return "44be8ab4-9740-45be-8107-fc1be13aff35";
	}

	@Override
	public Integer weight() {
		return 0;
	}

	@Override
	public boolean isVisible() {
		return Session.getOr().map(Session::isImpersontating).orElse(false);
	}

}

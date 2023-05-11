package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.jadaptive.api.session.Session;

@Component
public class SessionsMenu implements ApplicationMenu {

	@Override
	public String getI18n() {
		return Session.RESOURCE_KEY + ".names";
	}

	@Override
	public String getBundle() {
		return Session.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/search/" + Session.RESOURCE_KEY;
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList(Session.RESOURCE_KEY + ".read");
	}

	@Override
	public String getIcon() {
		return "fa-hourglass";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SECURITY_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "11794f5c-7d7a-4133-997d-a71b3f5a7fe4";
	}

	@Override
	public Integer weight() {
		return 30;
	}

}

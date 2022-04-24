package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.auth.AuthenticationPolicy;
import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class AuthenticationPolicyMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return AuthenticationPolicy.RESOURCE_KEY + ".names";
	}

	@Override
	public String getBundle() {
		return AuthenticationPolicy.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/search/" + AuthenticationPolicy.RESOURCE_KEY;
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList(AuthenticationPolicy.RESOURCE_KEY + ".read");
	}

	@Override
	public String getIcon() {
		return "far fa-shield";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.ADMINISTRATION_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "08e3a56c-b41a-42ef-887e-3b8f437f190d";
	}

	@Override
	public Integer weight() {
		return 99999;
	}

}

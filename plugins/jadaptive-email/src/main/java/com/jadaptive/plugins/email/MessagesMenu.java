package com.jadaptive.plugins.email;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class MessagesMenu implements ApplicationMenu {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public String getResourceKey() {
		return "messages.names";
	}

	@Override
	public String getBundle() {
		return Message.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/search/messages";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

	@Override
	public String getIcon() {
		return "typewriter";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.ADMINISTRATION_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "9f7c24fa-bbec-4e4d-b48b-65e313a9b293";
	}

	@Override
	public Integer weight() {
		return 10001;
	}

}

package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class EventsMenu implements ApplicationMenu {

	public EventsMenu() {
	}

	@Override
	public String getResourceKey() {
		return "eventsMenu.name";
	}

	@Override
	public String getBundle() {
		return "userInterface";
	}

	@Override
	public String getPath() {
		return "/app/ui/table/systemEvent";
	}

	@Override
	public String getIcon() {
		return "calendar-star";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}
	
	@Override
	public String getParent() {
		return ApplicationMenuService.REPORTING_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "c9e1cc19-bf1a-4281-8625-0ce33785a3e1";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

package com.jadaptive.plugins.email;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

//@Extension
public class HTMLTemplatesMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "htmlTemplates.names";
	}

	@Override
	public String getBundle() {
		return HTMLTemplate.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/table/htmlTemplates";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

	@Override
	public String getIcon() {
		return "file-code";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.ADMINISTRATION_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "abbfc544-075c-4563-a5d0-7a2e5d3dca40";
	}

	@Override
	public Integer weight() {
		return 10000;
	}

}

package com.jadaptive.plugins.sshd;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class SSHInterfacesMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "sshInterface.names";
	}

	@Override
	public String getBundle() {
		return SSHInterface.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/table/sshInterfaces";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

	@Override
	public String getIcon() {
		return "server";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "98ba0e52-7235-4663-b1d1-579e93fad49c";
	}

	@Override
	public Integer weight() {
		return 1000;
	}

}

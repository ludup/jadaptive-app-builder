package com.jadaptive.plugins.sshd;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class SSHDConfigurationMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "sshdConfiguration.name";
	}

	@Override
	public String getBundle() {
		return SSHDConfiguration.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/config/sshdConfiguration";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("sshdConfiguration.read");
	}

	@Override
	public String getIcon() {
		return "terminal";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "ebb98190-8a27-45bf-9288-de52d70cf0c2";
	}

	@Override
	public Integer weight() {
		return 500;
	}

}

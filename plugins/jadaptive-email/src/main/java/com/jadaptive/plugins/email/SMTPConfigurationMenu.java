package com.jadaptive.plugins.email;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.ui.menu.ApplicationMenu;
import com.jadaptive.api.ui.menu.ApplicationMenuService;

@Extension
public class SMTPConfigurationMenu implements ApplicationMenu {

	@Override
	public String getResourceKey() {
		return "smtpConfiguration.name";
	}
	
	public boolean isEnabled() { return true; }

	@Override
	public String getBundle() {
		return SMTPConfiguration.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/config/smtpConfiguration";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

	@Override
	public String getIcon() {
		return "envelope";
	}

	@Override
	public String getParent() {
		return ApplicationMenuService.SYSTEM_MENU_UUID;
	}

	@Override
	public String getUuid() {
		return "b3e69740-54c9-4133-897f-65f08c2c20a0";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

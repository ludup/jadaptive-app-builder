package com.jadaptive.plugins.web.ui.menus;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.config.ConfigurationPageItem;
import com.jadaptive.api.session.SessionConfiguration;

@Extension
public class SessionConfigurationMenu implements ConfigurationPageItem {

	@Override
	public String getResourceKey() {
		return "sessionConfiguration";
	}

	@Override
	public String getBundle() {
		return SessionConfiguration.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/config/sessionConfiguration";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("sessionConfiguration.read");
	}

	@Override
	public String getIcon() {
		return "hourglass-half";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

package com.jadaptive.api.ui.menu;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.stereotype.Component;

import com.jadaptive.api.config.ConfigurationPageItem;
import com.jadaptive.api.session.SessionConfiguration;

@Component
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
		return "fa-hourglass-half";
	}

	@Override
	public Integer weight() {
		return 0;
	}

}

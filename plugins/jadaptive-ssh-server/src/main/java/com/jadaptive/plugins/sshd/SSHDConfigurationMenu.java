package com.jadaptive.plugins.sshd;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.config.ConfigurationPageItem;

@Extension
public class SSHDConfigurationMenu implements ConfigurationPageItem {

	@Override
	public String getResourceKey() {
		return "sshdConfiguration";
	}

	@Override
	public String getBundle() {
		return SSHDConfiguration.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/system/sshdConfiguration";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("sshdConfiguration.read");
	}

	@Override
	public String getIcon() {
		return "fa-terminal";
	}

	@Override
	public Integer weight() {
		return 500;
	}
	
	@Override
	public boolean isSystem() {
		return true;
	}

}

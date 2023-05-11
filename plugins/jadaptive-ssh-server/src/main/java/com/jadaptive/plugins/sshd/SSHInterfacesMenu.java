package com.jadaptive.plugins.sshd;

import java.util.Arrays;
import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.config.ConfigurationPageItem;

@Extension
public class SSHInterfacesMenu implements ConfigurationPageItem {

	@Override
	public String getResourceKey() {
		return "sshInterface";
	}

	@Override
	public String getBundle() {
		return SSHInterface.RESOURCE_KEY;
	}

	@Override
	public String getPath() {
		return "/app/ui/search/sshInterfaces";
	}

	@Override
	public Collection<String> getPermissions() {
		return Arrays.asList("tenant.read");
	}

	@Override
	public String getIcon() {
		return "fa-ethernet";
	}

	@Override
	public Integer weight() {
		return 1000;
	}
	
	@Override
	public boolean isSystem() {
		return true;
	}

}

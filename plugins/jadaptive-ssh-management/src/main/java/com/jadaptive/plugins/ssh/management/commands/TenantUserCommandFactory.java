package com.jadaptive.plugins.ssh.management.commands;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.plugins.sshd.commands.PluginCommandFactory;

public class TenantUserCommandFactory extends PluginCommandFactory {

	@PostConstruct
	private void postConstruct() {
		installCommand("roles", Roles.class);
		installCommand("permissions", Permissions.class);
		installCommand("users", Users.class);
	}

	@Override
	public void assertAccess() throws AccessDeniedException {
		
	}
}
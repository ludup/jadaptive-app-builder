package com.jadaptive.sshd.commands;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class TenantUserCommandFactory extends AbstractAutowiredCommandFactory {

	@PostConstruct
	private void postConstruct() {
		installCommand("passwd", Passwd.class);
		installCommand("roles", Roles.class);
		installCommand("permissions", Permissions.class);
		installCommand("users", Users.class);
	}
}
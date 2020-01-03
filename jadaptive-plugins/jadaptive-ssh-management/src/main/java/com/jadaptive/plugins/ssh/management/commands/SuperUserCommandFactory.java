package com.jadaptive.plugins.ssh.management.commands;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class SuperUserCommandFactory extends AbstractAutowiredCommandFactory {

	@PostConstruct
	private void postConstruct() {
		installCommand("tenants", Tenants.class);
	}

}

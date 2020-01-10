package com.jadaptive.plugins.sshd.commands;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class UserCommandFactory extends AbstractAutowiredCommandFactory {

	@PostConstruct
	private void postConstruct() {
		installCommand("passwd", Passwd.class);
	}
}
package com.jadaptive.app.sshd.commands;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.jadaptive.api.sshd.commands.AbstractAutowiredCommandFactory;

@Component
public class UserCommandFactory extends AbstractAutowiredCommandFactory {

	@PostConstruct
	private void postConstruct() {
		installCommand("passwd", Passwd.class);
	}
}
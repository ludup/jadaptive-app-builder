package com.jadaptive.sshd.commands;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Component
public class UserCommandFactory extends CommandFactory<ShellCommand> {

	@Autowired
	ApplicationContext context;
	
	@PostConstruct
	private void postConstruct() {
		installCommand("passwd", Passwd.class);
		installCommand("roles", Roles.class);
	}
	
	protected void configureCommand(ShellCommand command, SshConnection con) throws IOException, PermissionDeniedException {
		
		context.getAutowireCapableBeanFactory().autowireBean(command);
	}
}

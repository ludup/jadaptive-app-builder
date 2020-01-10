package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

public class AbstractAutowiredCommandFactory extends CommandFactory<ShellCommand> {

	@Autowired
	ApplicationContext context;
	
	protected void configureCommand(ShellCommand command, SshConnection con) throws IOException, PermissionDeniedException {
		
		context.getAutowireCapableBeanFactory().autowireBean(command);
	}
}
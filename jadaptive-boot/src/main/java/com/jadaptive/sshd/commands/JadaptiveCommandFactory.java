package com.jadaptive.sshd.commands;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.jadaptive.permissions.PermissionService;
import com.jadaptive.user.UserService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.server.vsession.Command;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;
import com.sshtools.server.vsession.VirtualConsole;

@Component
public class JadaptiveCommandFactory extends CommandFactory<ShellCommand> {

	@Autowired
	ApplicationContext context;
	
	@Autowired
	PermissionService permissionService;
	
	@Autowired
	UserService userService; 
	
	@PostConstruct
	private void postConstruct() {
		installCommand("passwd", Passwd.class);
		installCommand("roles", Roles.class);
		installCommand("tenants", Tenants.class);
	}
	
	protected void configureCommand(ShellCommand command, SshConnection con) throws IOException, PermissionDeniedException {
		
		context.getAutowireCapableBeanFactory().autowireBean(command);
	}
}

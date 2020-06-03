package com.jadaptive.plugins.sshd.commands;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

public class AbstractAutowiredCommandFactory extends CommandFactory<ShellCommand> {

	static Logger log = LoggerFactory.getLogger(AbstractAutowiredCommandFactory.class);
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private PermissionService permissionService; 
	
	protected void configureCommand(ShellCommand command, SshConnection con) throws IOException, PermissionDeniedException {
		
		context.getAutowireCapableBeanFactory().autowireBean(command);
	}
	
	protected void tryCommand(String name, Class<? extends ShellCommand> clz, String... permissions) {
		try {
			if(permissions.length > 0) {
				permissionService.assertAnyPermission(permissions);
			}
			installCommand(name, clz);
		} catch(AccessDeniedException e) {
			log.info("{} will not be available to {}", name, permissionService.getCurrentUser().getUsername());
		}
	}
}
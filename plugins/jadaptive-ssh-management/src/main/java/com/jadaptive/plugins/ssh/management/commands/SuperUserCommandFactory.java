package com.jadaptive.plugins.ssh.management.commands;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class SuperUserCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory { 

	@Autowired
	private PermissionService permissionService; 
	
	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		permissionService.assertAdministrator();
		installCommand("updates", Updates.class);
		installCommand("shutdown", Shutdown.class);
		installCommand("restart", Restart.class);
		return this;
	}

}

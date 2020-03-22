package com.jadaptive.plugins.ssh.management.commands;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;
import com.sshtools.server.vsession.commands.admin.Connections;
import com.sshtools.server.vsession.jvm.Mem;
import com.sshtools.server.vsession.jvm.ThreadDump;
import com.sshtools.server.vsession.jvm.Threads;

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
		installCommand("con", Connections.class);
		installCommand("mem", Mem.class);
		installCommand("threads", Threads.class);
		installCommand("thread-dump", ThreadDump.class);
		return this;
	}

}

package com.jadaptive.plugins.ssh.vsftp.commands;

import org.pf4j.Extension;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.sshd.PluginCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class VirtualFileCommandFactory implements PluginCommandFactory {

	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		// TODO Auto-generated method stub
		return null;
	}

}

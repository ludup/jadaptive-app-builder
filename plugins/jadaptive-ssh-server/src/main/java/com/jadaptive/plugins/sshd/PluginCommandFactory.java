package com.jadaptive.plugins.sshd;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

public interface PluginCommandFactory extends ExtensionPoint {

	CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException;

}

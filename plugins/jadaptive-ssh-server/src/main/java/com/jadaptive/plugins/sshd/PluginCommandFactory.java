package com.jadaptive.plugins.sshd;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

public interface PluginCommandFactory {

	CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException;

}

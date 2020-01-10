package com.jadaptive.plugins.sshd.commands;

import org.pf4j.ExtensionPoint;

import com.jadaptive.api.permissions.AccessDeniedException;

public abstract class PluginCommandFactory extends AbstractAutowiredCommandFactory implements ExtensionPoint {

	public abstract void assertAccess() throws AccessDeniedException;
}

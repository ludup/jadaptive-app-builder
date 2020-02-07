package com.jadaptive.plugins.ssh.management.commands;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.plugins.sshd.PluginCommandFactory;
import com.jadaptive.plugins.sshd.commands.AbstractAutowiredCommandFactory;
import com.sshtools.server.vsession.CommandFactory;
import com.sshtools.server.vsession.ShellCommand;

@Extension
public class SuperUserCommandFactory extends AbstractAutowiredCommandFactory implements PluginCommandFactory {

	@Autowired
	TenantService tenantService; 

	@Override
	public CommandFactory<ShellCommand> buildFactory() throws AccessDeniedException {
		tenantService.assertManageTenant();
		installCommand("tenants", Tenants.class);
		return this;
	}

}

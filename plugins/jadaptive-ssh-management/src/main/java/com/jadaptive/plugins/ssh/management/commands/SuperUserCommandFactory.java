package com.jadaptive.plugins.ssh.management.commands;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.plugins.sshd.commands.PluginCommandFactory;

public class SuperUserCommandFactory extends PluginCommandFactory {

	@Autowired
	TenantService tenantService; 
	
	@PostConstruct
	private void postConstruct() {
		installCommand("tenants", Tenants.class);
	}

	@Override
	public void assertAccess() throws AccessDeniedException {
		tenantService.assertManageTenant();
	}

}

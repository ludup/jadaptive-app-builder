package com.jadaptive.plugins.ssh.management.commands.tenants;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class SwitchTenant extends AbstractTenantAwareCommand {

	@Autowired
	TenantService tenantService; 
	
	public SwitchTenant() {
		super("switch-tenant", "System Management",
				UsageHelper.build("switch-tenant [domain]"),
				"Switch to manage another tenant");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length < 2) {
			console.println("Invalid arguments!");
			printUsage();
			return;
		}
			
		Tenant tenant = resolveTenant(args[args.length-1]);
		
		tenantService.assertManageTenant();
		console.getEnvironment().put("TENANT_NAME", tenant.getName());
		console.getEnvironment().put("TENANT_UUID", tenant.getUuid());
		console.println(String.format("You are now managing %s", tenant.getName()));

	}

	private Tenant resolveTenant(String name) {
		return tenantService.getTenantByDomain(name);
	}
}

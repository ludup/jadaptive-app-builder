package com.jadaptive.plugins.ssh.management.commands.tenants;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class DeleteTenant extends AbstractTenantAwareCommand {

	@Autowired
	TenantService tenantService; 
	
	public DeleteTenant() {
		super("delete-tenant", "System Management", 
				UsageHelper.build("delete-tenant [domain|name]"), 
				"Delete a tenant domain");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length < 2) {
			console.println("Invalid arguments!");
			printUsage();
			return;
		}
			
		String domain = args[args.length-1];

		Tenant tenant;
		
		try {
			tenant = tenantService.getTenantByDomain(domain);
		} catch(ObjectNotFoundException e) {
			tenant = tenantService.getTenantByName(domain);
		}
		
		tenantService.deleteTenant(tenant);
		console.println(String.format("Deleted tenant %s", domain));
	}

}

package com.jadaptive.sshd.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.tenant.Tenant;
import com.jadaptive.tenant.TenantService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Tenants extends AbstractCommand {

	@Autowired
	TenantService tenantService; 
	
	public Tenants() {
		super("tenants", "System", "tenant [options] [tenant]", "Manage tenants");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length==1) {
			printTenants();
		} else if(CliHelper.hasShortOption(args, 'c') || CliHelper.hasLongOption(args, "create") && args.length == 4) {
			
			String name = args[2];
			String hostname = args[3];
			tenantService.createTenant(name, hostname);
			
		} else if(args.length >= 3) {
			
			Tenant tenant = resolveTenant(args[2]);
			
			switch(args[1]) {
			case "-m":
			case "--manage":
				console.getEnvironment().put("TENANT_NAME", tenant.getName());
				console.getEnvironment().put("TENANT_UUID", tenant.getUuid());
				break;
			case "-d":
			case "--delete":
				tenantService.deleteTenant(tenant);
				break;
			default:
				break;
			}
		}
	}

	private Tenant resolveTenant(String name) {
		return tenantService.getTenantByName(name);
	}

	private void printTenants() {
		
		for(Tenant tenant : tenantService.listTenants()) {
			console.println(tenant.getName());
		}
		
	}

}

package com.jadaptive.plugins.ssh.management.commands.tenants;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Tenants extends AbstractTenantAwareCommand {

	@Autowired
	TenantService tenantService; 
	
	public Tenants() {
		super("tenants", "System Management", UsageHelper.build("tenant [options] [domain]",
				"-l , --list [-f, --friendly]   List tenant domains",
				"-m, --manage <domain>          Switch to the named domain"), "List tenant domains");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length==1 || (CliHelper.hasShortOption(args, 'l') || CliHelper.hasLongOption(args, "list"))) {
			printTenants(CliHelper.hasShortOption(args, 'f') || CliHelper.hasLongOption(args, "friendly"));
		} else if(CliHelper.hasShortOption(args, 'c') || CliHelper.hasLongOption(args, "create") && args.length == 4) {
			
			String name = args[3];
			String hostname = args[2];
			tenantService.createTenant(name, hostname);
			
		} else if(args.length >= 3) {
			
			Tenant tenant = resolveTenant(args[2]);
			
			if(CliHelper.hasOption(args, 'm', "manage")) {
				tenantService.assertManageTenant();
				console.getEnvironment().put("TENANT_NAME", tenant.getName());
				console.getEnvironment().put("TENANT_UUID", tenant.getUuid());
				console.println(String.format("You are now managing %s", tenant.getName()));
			}

		} else {
			console.println("Invalid arguments!");
			printUsage();
		}
	}

	private Tenant resolveTenant(String name) {
		return tenantService.getTenantByDomain(name);
	}

	private void printTenants(boolean friendly) {
		
		for(Tenant tenant : tenantService.listTenants()) {
			if(friendly) {
				console.println(String.format("%s (%s)", tenant.getName(), tenant.getDomain()));
			} else {
				console.println(tenant.getDomain());
			}
		}
		
	}

}

package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Tenants extends AbstractTenantAwareCommand {

	@Autowired
	TenantService tenantService; 
	
	public Tenants() {
		super("tenants", "System", UsageHelper.build("tenant [options] [domain]",
				"-l , --list [-f, --friendly]   List tenant domains",
				"-m, --manage <domain>          Switch to the named domain",
				"-c, --create <domain> <name>   Create a new Tenant",
				"-d, --delete <domain>          Delete an existing Tenant"), "Manage tenant domains");
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
			
			switch(args[1]) {
			case "-m":
			case "--manage":
				tenantService.assertManageTenant();
				console.getEnvironment().put("TENANT_NAME", tenant.getName());
				console.getEnvironment().put("TENANT_UUID", tenant.getUuid());
				console.println(String.format("You are now managing %s", tenant.getName()));
				break;
			case "-d":
			case "--delete":
				String answer = console.readLine(String.format("Are you sure you want to delete the tenant %s [y/n]: ", tenant.getName()));
				switch(answer.trim()) {
				case "y":
				case "yes":
					tenantService.deleteTenant(tenant);
					console.println(String.format("%s was deleted", tenant.getName()));
					break;
				default:
					console.println(String.format("%s was not deleted", tenant.getName()));
				}
				
				break;
			default:
				break;
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
				console.println(String.format("%s (%s)", tenant.getName(), tenant.getHostname()));
			} else {
				console.println(tenant.getHostname());
			}
		}
		
	}

}

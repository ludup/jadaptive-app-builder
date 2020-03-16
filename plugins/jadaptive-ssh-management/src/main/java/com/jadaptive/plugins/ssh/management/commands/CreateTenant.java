package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.plugins.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.CliHelper;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class CreateTenant extends AbstractTenantAwareCommand {

	@Autowired
	TenantService tenantService; 
	
	public CreateTenant() {
		super("create-tenant", "System Management", UsageHelper.build("create-tenant [domain]",
				"-n, --name  <name>   Create a new Tenant"), "Create a tenant domain");
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

		String name;
		if(!CliHelper.hasOption(args, 'n', "name")) {
			name = console.readLine("Name: ");
		} else {
			name = CliHelper.getValue(args, 'n', "name");
		}
		
		tenantService.createTenant(name, domain);
		console.println(String.format("Created tenant %s", name));
	}

}

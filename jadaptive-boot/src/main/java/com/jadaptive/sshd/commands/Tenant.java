package com.jadaptive.sshd.commands;

import java.io.IOException;

import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Tenant extends AbstractCommand {

	public Tenant() {
		super("tenant", "System", "tenant [options] ", "Manage tenants");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		if(args.length==1) {
			console.println(tenantService.getCurrentTenant().getName());
		}

	}

}

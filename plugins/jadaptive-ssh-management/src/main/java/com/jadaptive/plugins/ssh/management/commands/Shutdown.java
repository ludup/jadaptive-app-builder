package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import com.jadaptive.plugins.sshd.commands.UserCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Shutdown extends UserCommand {
	
	public Shutdown() {
		super("shutdown", "System Management", UsageHelper.build("shutdown"),
				"Shutdown the application");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		String answer = console.readLine("Shutdown the application? (y/n): ");
		if("yes".contains(answer.toLowerCase())) {
			System.exit(0);
		}
	}

}

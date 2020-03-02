package com.jadaptive.plugins.ssh.management.commands;

import java.io.IOException;

import com.jadaptive.plugins.sshd.commands.UserCommand;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.UsageHelper;
import com.sshtools.server.vsession.VirtualConsole;

public class Restart extends UserCommand {
	
	public Restart() {
		super("restart", "System Management", UsageHelper.build("restart"),
				"Restart the application");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		String answer = console.readLine("Restart the application? (y/n): ");
		if("yes".contains(answer.toLowerCase())) {
			System.exit(0xF000);
		}
	}

}

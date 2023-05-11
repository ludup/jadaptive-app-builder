package com.jadaptive.plugins.sshd;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.UserService;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.server.vsession.Command;
import com.sshtools.server.vsession.MshListener;
import com.sshtools.server.vsession.RootShell;
import com.sshtools.server.vsession.ShellCommandFactory;
import com.sshtools.server.vsession.VirtualConsole;
import com.sshtools.server.vsession.VirtualShellNG;

public class VirtualShell extends VirtualShellNG {

	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private UserService userService; 
	
	public VirtualShell(SshConnection con, ShellCommandFactory commandFactory) {
		super(con, commandFactory);
	}
	
	@Override
	protected RootShell createShell(SshConnection con) throws PermissionDeniedException, IOException {
		RootShell shell = super.createShell(con);
		shell.addListener(new MshListener() {
			
			@Override
			public void started(String[] args, VirtualConsole console) {
				String user = (String) console.getEnvironment().get("USER");
				permissionService.setupUserContext(userService.getUser(user));
			}
			
			@Override
			public void finished(String[] args, VirtualConsole console) {
				permissionService.clearUserContext();
			}
			
			@Override
			public void commandStarted(Command cmd, String[] args, VirtualConsole console) {
				String user = (String) console.getEnvironment().get("USER");
				permissionService.setupUserContext(userService.getUser(user));
			}

			@Override
			public void commandFinished(Command cmd, String[] args, VirtualConsole console) {
				permissionService.clearUserContext();
			}
		});
		return shell;
	}

}

package com.jadaptive.plugins.sshd;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tenant.Tenant;
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

	
	@Override
	protected RootShell createShell(SshConnection con) throws PermissionDeniedException, IOException {
		RootShell shell = super.createShell(con);
		shell.addListener(new MshListener() {
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

	@Autowired
	PermissionService permissionService; 
	
	@Autowired
	UserService userService; 
	
	public VirtualShell(SshConnection con, ShellCommandFactory commandFactory) {
		super(con, commandFactory);
		Tenant tenant = (Tenant) con.getProperty(SSHDService.TENANT);
		setEnvironmentVariable("TENANT_NAME", tenant.getName());
		setEnvironmentVariable("TENANT_UUID", tenant.getUuid());
		setEnvironmentVariable("USER", con.getUsername());
		
		
	}


}

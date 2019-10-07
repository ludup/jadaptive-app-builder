package com.jadaptive.sshd;

import com.jadaptive.tenant.Tenant;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.server.vsession.ShellCommandFactory;
import com.sshtools.server.vsession.VirtualShellNG;

public class VirtualShell extends VirtualShellNG {

	public VirtualShell(SshConnection con, ShellCommandFactory commandFactory) {
		super(con, commandFactory);
		Tenant tenant = (Tenant) con.getProperty(SSHDService.TENANT);
		setEnvironmentVariable("TENANT_NAME", tenant.getName());
		setEnvironmentVariable("TENANT_UUID", tenant.getUuid());
	}

}

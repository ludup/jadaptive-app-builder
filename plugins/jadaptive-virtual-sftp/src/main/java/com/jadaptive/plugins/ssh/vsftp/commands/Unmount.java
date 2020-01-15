package com.jadaptive.plugins.ssh.vsftp.commands;

import java.io.IOException;

import com.jadaptive.api.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.FileSystemPolicy;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class Unmount extends AbstractVFSCommand {

	public Unmount() {
		super("umount", "VFS", "", "");
	}

	@Override
	protected void doRun(String[] args, VirtualConsole console)
			throws IOException, PermissionDeniedException, UsageException {
		
		
		
		
		
	}

}

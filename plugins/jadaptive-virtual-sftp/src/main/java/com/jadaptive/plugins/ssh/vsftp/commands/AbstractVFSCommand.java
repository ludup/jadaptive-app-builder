package com.jadaptive.plugins.ssh.vsftp.commands;

import com.jadaptive.api.sshd.commands.AbstractTenantAwareCommand;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.policy.FileSystemPolicy;

public abstract class AbstractVFSCommand extends AbstractTenantAwareCommand {

	public AbstractVFSCommand(String name, String subsystem, String signature, String description) {
		super(name, subsystem, signature, description);
	}
	
	
	protected VirtualFileFactory getFileFactory() {
		return (VirtualFileFactory) 
				console.getContext().getPolicy(FileSystemPolicy.class)
					.getFileFactory(console.getConnection());
	}


}

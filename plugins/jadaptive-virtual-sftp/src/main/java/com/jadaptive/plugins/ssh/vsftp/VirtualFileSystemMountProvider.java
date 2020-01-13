package com.jadaptive.plugins.ssh.vsftp;

import java.util.Collection;

import org.pf4j.Extension;

import com.jadaptive.api.sshd.PluginFileSystemMount;
import com.sshtools.common.files.vfs.VirtualMountTemplate;

@Extension
public class VirtualFileSystemMountProvider implements PluginFileSystemMount {

	@Override
	public Collection<? extends VirtualMountTemplate> getAdditionalMounts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasHome() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public VirtualMountTemplate getHomeMount() {
		// TODO Auto-generated method stub
		return null;
	}

}

package com.jadaptive.plugins.sshd;

import java.util.Collection;

import com.sshtools.common.files.vfs.VirtualMountTemplate;

public interface PluginFileSystemMount {

	Collection<? extends VirtualMountTemplate> getAdditionalMounts();

	boolean hasHome();

	VirtualMountTemplate getHomeMount();

}

package com.jadaptive.api.sshd;

import java.util.Collection;

import org.pf4j.ExtensionPoint;

import com.sshtools.common.files.vfs.VirtualMountTemplate;

public interface PluginFileSystemMount extends ExtensionPoint {

	Collection<? extends VirtualMountTemplate> getAdditionalMounts();

	boolean hasHome();

	VirtualMountTemplate getHomeMount();

}

package com.jadaptive.plugins.sshd;

import org.pf4j.ExtensionPoint;

import com.sshtools.common.nio.ProtocolContextFactory;

public interface SshInterface extends ExtensionPoint {

	String getAddressToBind();

	int getPort();

	ProtocolContextFactory<?> getContextFactory();

}

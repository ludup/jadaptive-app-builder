package com.jadaptive.plugins.sshd;

import com.sshtools.common.nio.ProtocolContextFactory;

public interface SSHInterface {

	String getAddressToBind();

	int getPort();

	ProtocolContextFactory<?> getContextFactory();

	default String getInterface() { return String.format("%s:%d", getAddressToBind(), getPort()); }

}

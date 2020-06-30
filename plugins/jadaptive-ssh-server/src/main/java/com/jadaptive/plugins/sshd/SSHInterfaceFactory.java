package com.jadaptive.plugins.sshd;

import org.pf4j.ExtensionPoint;

public interface SSHInterfaceFactory extends ExtensionPoint {

	Iterable<SSHInterface> getInterfaces();
}

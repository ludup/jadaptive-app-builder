package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.pf4j.ExtensionPoint;

import com.sshtools.common.ssh.SshException;
import com.sshtools.server.SshServerContext;
import com.sshtools.synergy.nio.SshEngineContext;

public interface SSHInterfaceFactory extends ExtensionPoint {

	SshServerContext createContext(SshEngineContext daemonContext, SocketChannel sc, SSHInterface intf)
			throws IOException, SshException;
}

package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.pf4j.ExtensionPoint;

import com.sshtools.common.ssh.SshException;
import com.sshtools.synergy.nio.ProtocolContext;
import com.sshtools.synergy.nio.SshEngineContext;

public interface SSHInterfaceFactory<T extends ProtocolContext,S extends SSHInterface> extends ExtensionPoint {

	T createContext(SshEngineContext daemonContext, SocketChannel sc, S intf)
			throws IOException, SshException;
}

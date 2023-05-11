package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.sshtools.common.ssh.SshException;
import com.sshtools.synergy.nio.ProtocolContext;
import com.sshtools.synergy.nio.ProtocolContextFactory;
import com.sshtools.synergy.nio.SshEngineContext;

public class SSHDInterface<T extends ProtocolContext,S extends SSHInterface> implements ProtocolContextFactory<T> {

	SSHInterfaceFactory<T,S> factory;
	SSHInterface iface;
	
	public SSHDInterface(SSHInterfaceFactory<T,S> factory, SSHInterface iface) {
		super();
		this.factory = factory;
		this.iface = iface;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T createContext(SshEngineContext daemonContext, SocketChannel sc)
			throws IOException, SshException {
		return factory.createContext(daemonContext, sc, (S)iface);
	}

}

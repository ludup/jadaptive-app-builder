package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.sshtools.common.ssh.SshException;
import com.sshtools.server.SshServerContext;
import com.sshtools.synergy.nio.ProtocolContextFactory;
import com.sshtools.synergy.nio.SshEngineContext;

public class SSHDInterface implements ProtocolContextFactory<SshServerContext> {

	SSHInterfaceFactory factory;
	SSHInterface iface;
	
	public SSHDInterface(SSHInterfaceFactory factory, SSHInterface iface) {
		super();
		this.factory = factory;
		this.iface = iface;
	}
	
	@Override
	public SshServerContext createContext(SshEngineContext daemonContext, SocketChannel sc)
			throws IOException, SshException {
		return factory.createContext(daemonContext, sc, iface);
	}

}

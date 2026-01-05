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
	
	/**
	 * Different callback usages may pass additional information in the
	 * username such as originating user, tenant, and agent name. If the agent
	 * is always for a system tenant for example, it  may only need an agent name.
	 * If its for a multi-tenanted environment where the port is shared amongst tenants,
	 * we may need to provide the tenant as well. Some types may also support the username
	 * as was used with initial authorization for additional security checks.
	 * 
	 * @param userSpec interface specific user spec
	 * @return user spec
	 */
	default UserSpec userSpec(String userSpec) {
		return new UserSpec.Builder().
				withDevice(userSpec).
				build();
	}
}

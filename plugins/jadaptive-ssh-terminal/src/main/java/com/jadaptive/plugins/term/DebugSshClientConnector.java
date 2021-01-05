package com.jadaptive.plugins.term;

import java.io.IOException;

import com.sshtools.client.SshClient;
import com.sshtools.common.ssh.SshException;

public class DebugSshClientConnector implements SshClientConnector {

	@Override
	public SshClient connect(String sessionId) throws IOException, SshException {
		return new SshClient("localhost", 22, "lee", "p13av#pq23".toCharArray());
	}

}

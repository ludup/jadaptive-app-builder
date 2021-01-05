package com.jadaptive.plugins.term;

import java.io.IOException;

import com.sshtools.client.SshClient;
import com.sshtools.common.ssh.SshException;

public interface SshClientConnector {

	SshClient connect(String sessionId) throws IOException, SshException;
}

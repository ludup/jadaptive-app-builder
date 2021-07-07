package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.net.UnknownHostException;

import com.jadaptive.api.user.User;
import com.sshtools.common.files.AbstractFileFactory;
import com.sshtools.server.SshServerContext;
import com.sshtools.synergy.nio.SshEngine;

public interface SSHDService {

	public static final String USER = "userObject";
	public static final String TENANT = "tenantObject";
	
	AbstractFileFactory<?> getFileFactory(User user);

	void addInterface(SSHInterface sshInterface) throws IOException;

	boolean isListening(String intf);

	SSHInterface getInterface(String intf);

	void removeInterface(SSHInterface sshInterface) throws UnknownHostException;

	SshEngine getEngine();

	void applyConfiguration(SshServerContext sshContext);
	
	boolean isRunning();
	
	void start(boolean requireValidInterface) throws IOException;

}

package com.jadaptive.sshd;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.app.ApplicationProperties;
import com.jadaptive.app.ApplicationVersion;
import com.jadaptive.sshd.commands.UserCommandFactory;
import com.sshtools.common.files.vfs.VFSFileFactory;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.files.vfs.VirtualMountTemplate;
import com.sshtools.common.ssh.SshException;
import com.sshtools.server.SshServer;
import com.sshtools.server.SshServerContext;
import com.sshtools.server.vsession.VirtualChannelFactory;
import com.sshtools.server.vsession.VirtualSessionPolicy;
import com.sshtools.server.vshell.commands.fs.FileSystemCommandFactory;

@Service
public class SSHDServiceImpl extends SshServer implements SSHDService {

	static Logger log = LoggerFactory.getLogger(SSHDServiceImpl.class);

	@Autowired
	PasswordAuthenticatorImpl passwordAuthenticator; 
	
	@Autowired
	UserCommandFactory userComands; 
	
	public SSHDServiceImpl() throws UnknownHostException {
		super();
	}

	@PostConstruct
	private void postConstruct() {

		try {

			int port = ApplicationProperties.getValue("sshd.port", 2222);
			boolean extenalAccess = ApplicationProperties.getValue("sshd.externalAccess", false);

			addAuthenticator(passwordAuthenticator);

			setFileFactory(new VirtualFileFactory(new VirtualMountTemplate("/", "tmp://", new VFSFileFactory()),
					new VirtualMountTemplate("/conf", "conf", new VFSFileFactory())));

			addInterface(extenalAccess ? "::" : "::1", port);
			addInterface(extenalAccess ? "0.0.0.0" : "127.0.0.1", port);

			start(true);
		} catch (IOException e) {
			log.error("SSHD service failed to start", e);
		}
	}

	protected void configureChannels(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		sshContext.setChannelFactory(new VirtualChannelFactory(
				new FileSystemCommandFactory(),
				userComands
		));

		StringBuffer out = new StringBuffer();
		out.append("   _           _             _   _           \n");
		out.append("  (_) __ _  __| | __ _ _ __ | |_(_)_   _____ \n");
		out.append("  | |/ _` |/ _` |/ _` | '_ \\| __| \\ \\ / / _ \\\n");
		out.append("  | | (_| | (_| | (_| | |_) | |_| |\\ V /  __/\n");
		out.append(" _/ |\\__,_|\\__,_|\\__,_| .__/ \\__|_| \\_/ \\___|\n");
		out.append("|__/                  |_|                    \n");
		out.append("==== ");
		out.append(String.format("Version %s", ApplicationVersion.getVersion()));
		out.append("\n\n");
		
		sshContext.getPolicy(VirtualSessionPolicy.class).setWelcomeText(out.toString());
	}
}

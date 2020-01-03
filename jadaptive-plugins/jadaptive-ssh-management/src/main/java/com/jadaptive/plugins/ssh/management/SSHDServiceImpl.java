package com.jadaptive.plugins.ssh.management;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.ssh.management.commands.SuperUserCommandFactory;
import com.jadaptive.plugins.ssh.management.commands.TenantUserCommandFactory;
import com.sshtools.common.files.vfs.VFSFileFactory;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.files.vfs.VirtualMountTemplate;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.ClassLoaderPolicy;
import com.sshtools.common.ssh.ChannelNG;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.UnsupportedChannelException;
import com.sshtools.server.SshServer;
import com.sshtools.server.SshServerContext;
import com.sshtools.server.vsession.ShellCommandFactory;
import com.sshtools.server.vsession.VirtualChannelFactory;
import com.sshtools.server.vsession.VirtualSessionPolicy;
import com.sshtools.server.vshell.commands.fs.FileSystemCommandFactory;

@Service
public class SSHDServiceImpl extends SshServer implements SSHDService {

	static Logger log = LoggerFactory.getLogger(SSHDServiceImpl.class);

	@Autowired
	private PasswordAuthenticatorImpl passwordAuthenticator; 
	
	@Autowired
	private SuperUserCommandFactory superUserCommands; 
	
	@Autowired
	private TenantUserCommandFactory tenantUserCommands; 
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private UserService userService; 
	
	
	public SSHDServiceImpl() throws UnknownHostException {
		super();
	}

	@PostConstruct
	private void postConstruct() {

		try {

			int port = ApplicationProperties.getValue("sshd.port", 2222);
			boolean extenalAccess = ApplicationProperties.getValue("sshd.externalAccess", false);

			addAuthenticator(passwordAuthenticator);

			addInterface(extenalAccess ? "::" : "::1", port);
			addInterface(extenalAccess ? "0.0.0.0" : "127.0.0.1", port);

			start(true);
		} catch (IOException e) {
			log.error("SSHD service failed to start", e);
		}
	}

	@Override
	protected void configureFilesystem(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		VirtualFileFactory vff = new VirtualFileFactory(
				new VirtualMountTemplate("/", "tmp://", new VFSFileFactory())); 
		
		sshContext.setFileFactory(vff);
	}
	
	protected void configureChannels(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		sshContext.setChannelFactory(new VirtualChannelFactory() {

			@Override
			protected ChannelNG<SshServerContext> createSessionChannel(SshConnection con)
					throws UnsupportedChannelException, PermissionDeniedException {
				
				ShellCommandFactory scf = new ShellCommandFactory(
						new FileSystemCommandFactory(),
						tenantUserCommands);
				
				permissionService.setupUserContext(userService.findUsername(con.getUsername()));
				
				try {
					if(tenantService.getCurrentTenant().getSystem()) {
						try {
							tenantService.assertManageTenant();
							scf.installFactory(superUserCommands);
						} catch(AccessDeniedException e) { }
					} 
					
					VirtualShell shell = new VirtualShell(con, scf);
					
					context.getAutowireCapableBeanFactory().autowireBean(shell);
					
					return shell;
				
				} finally {
					permissionService.clearUserContext();
				}
			}
			
		});

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
		ClassLoader classLoader = getClass().getClassLoader();
		sshContext.getPolicy(ClassLoaderPolicy.class).setClassLoader(classLoader);
	}
}

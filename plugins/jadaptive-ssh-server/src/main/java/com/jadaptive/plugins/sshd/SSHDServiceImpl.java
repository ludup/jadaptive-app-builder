package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.UserCommandFactory;
import com.sshtools.common.files.AbstractFileFactory;
import com.sshtools.common.files.vfs.VFSFileFactory;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.files.vfs.VirtualMountTemplate;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.ClassLoaderPolicy;
import com.sshtools.common.policy.FileSystemPolicy;
import com.sshtools.common.scp.ScpCommand;
import com.sshtools.common.ssh.ChannelNG;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.UnsupportedChannelException;
import com.sshtools.server.SshServer;
import com.sshtools.server.SshServerContext;
import com.sshtools.server.vsession.ShellCommandFactory;
import com.sshtools.server.vsession.VirtualChannelFactory;
import com.sshtools.server.vsession.VirtualSessionPolicy;
import com.sshtools.server.vsession.commands.fs.FileSystemCommandFactory;

@Service
public class SSHDServiceImpl extends SshServer implements SSHDService, StartupAware {

	static Logger log = LoggerFactory.getLogger(SSHDServiceImpl.class);

	@Autowired
	private ApplicationService appContext; 
	
	@Autowired
	private PasswordAuthenticatorImpl passwordAuthenticator; 
	
	@Autowired
	private AuthorizedKeyProvider authorizedKeysAuthenticator;
	
	@Autowired
	private UserCommandFactory userCommands; 
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private UserService userService; 
	
	public SSHDServiceImpl() throws UnknownHostException {
		super();
	}

	@Override
	public void onApplicationStartup() {

		try {

			int port = ApplicationProperties.getValue("sshd.port", 2222);
			boolean extenalAccess = ApplicationProperties.getValue("sshd.externalAccess", true);

			if(ApplicationProperties.getValue("sshd.permitPassword", true)) {
				addAuthenticator(passwordAuthenticator);
			}
			
			addAuthenticator(authorizedKeysAuthenticator);
			
			addInterface(extenalAccess ? "::" : "::1", port);

			start(true);
		} catch (IOException e) {
			log.error("SSHD service failed to start", e);
		}
	}
	
	@Override
	public Integer getStartupPosition() {
		return 0;
	}

	@Override
	protected void configureFilesystem(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		try {
			sshContext.setPolicy(FileSystemPolicy.class, new FileSystemPolicy() {

				@Override
				public AbstractFileFactory<?> getFileFactory(SshConnection con) {
					
					AbstractFileFactory<?> ff = super.getFileFactory(con);
					if(Objects.nonNull(ff)) {
						return ff;
					}
					
					permissionService.setupUserContext(userService.getUser(con.getUsername()));
					
					try {
						List<VirtualMountTemplate> mounts = new ArrayList<>();
						VirtualMountTemplate home = null;
						for(PluginFileSystemMount mount : appContext.getBeans(PluginFileSystemMount.class)) {
							mounts.addAll(mount.getAdditionalMounts());
							if(mount.hasHome()) {
								if(Objects.nonNull(home)) {
									if(log.isWarnEnabled()) {
										log.warn("A plugin attempted to configure a home mount but it was already defined.");
									}
									continue;
								}
								home = mount.getHomeMount();
							}
						}
						
						if(Objects.isNull(home)) {
							home = new VirtualMountTemplate("/", "tmp://", new VFSFileFactory(), true);
						}

						super.setFileFactory(new VirtualFileFactory(
								home, mounts.toArray(new VirtualMountTemplate[0])));
						
						return super.getFileFactory(con);
					} catch (IOException e) {
						throw new IllegalStateException(e.getMessage(), e);
					} finally {
						permissionService.clearUserContext();
					}
				}
				
			});
			


		
		} finally {
			
		}
	}
	
	protected void configureChannels(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		sshContext.setIdleConnectionTimeoutSeconds(60*15);
		sshContext.addCommand("scp", ScpCommand.class);
		sshContext.setChannelFactory(new VirtualChannelFactory() {

			@Override
			protected ChannelNG<SshServerContext> createSessionChannel(SshConnection con)
					throws UnsupportedChannelException, PermissionDeniedException {
				
				ShellCommandFactory scf = new ShellCommandFactory(
						new FileSystemCommandFactory(),
						userCommands);
				
				permissionService.setupUserContext(userService.getUser(con.getUsername()));
				
				try {
					
					for(PluginCommandFactory cf : appContext.getBeans(PluginCommandFactory.class)) {
						try {
							scf.installFactory(cf.buildFactory());
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
		out.append("==============================================\n");
		out.append("   _           _             _   _           \n");
		out.append("  (_) __ _  __| | __ _ _ __ | |_(_)_   _____ \n");
		out.append("  | |/ _` |/ _` |/ _` | '_ \\| __| \\ \\ / / _ \\\n");
		out.append("  | | (_| | (_| | (_| | |_) | |_| |\\ V /  __/\n");
		out.append(" _/ |\\__,_|\\__,_|\\__,_| .__/ \\__|_| \\_/ \\___|\n");
		out.append("|__/                  |_|                    \n");
		out.append("\n");
		out.append(System.getProperty("jadaptive.productName", "Adaptive Application Builder"));
		out.append("\n");
		out.append(ApplicationVersion.getVersion());
		out.append("\n==============================================\n");
		out.append("\nType 'help' for a list of commands.\n");
		
		sshContext.getPolicy(VirtualSessionPolicy.class).setWelcomeText(out.toString());
		ClassLoader classLoader = getClass().getClassLoader();
		sshContext.getPolicy(ClassLoaderPolicy.class).setClassLoader(classLoader);
	}
}

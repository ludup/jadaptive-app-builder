package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationUpdateManager;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.UserCommandFactory;
import com.sshtools.common.files.AbstractFileFactory;
import com.sshtools.common.files.vfs.VFSFileFactory;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.files.vfs.VirtualMountTemplate;
import com.sshtools.common.forwarding.ForwardingPolicy;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.ClassLoaderPolicy;
import com.sshtools.common.policy.FileFactory;
import com.sshtools.common.policy.FileSystemPolicy;
import com.sshtools.common.scp.ScpCommand;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.UnsupportedChannelException;
import com.sshtools.server.SshServer;
import com.sshtools.server.SshServerContext;
import com.sshtools.server.vsession.ShellCommandFactory;
import com.sshtools.server.vsession.VirtualChannelFactory;
import com.sshtools.server.vsession.VirtualSessionPolicy;
import com.sshtools.server.vsession.commands.fs.FileSystemCommandFactory;
import com.sshtools.synergy.nio.SshEngineContext;
import com.sshtools.synergy.ssh.ChannelNG;
import com.sshtools.vsession.commands.ssh.SshClientsCommandFactory;


@Service
public class SSHDServiceImpl extends SshServer implements SSHDService, StartupAware {

	static Logger log = LoggerFactory.getLogger(SSHDServiceImpl.class);

	@Autowired
	private ApplicationService appContext; 
	
	@Autowired
	private UserCommandFactory userCommands; 
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private PermissionService permissionService; 
	
	@Autowired
	private ApplicationUpdateManager updateManager; 
	
	@Autowired
	private UserService userService; 
	
	@Autowired
	private SingletonObjectDatabase<SSHDConfiguration> configService;
	
	@Autowired
	private SSHInterfaceService interfaceService; 
	
	Map<String,SSHInterface> interfaces = new HashMap<>();
	
	public SSHDServiceImpl() throws UnknownHostException {
		super();
	}

	@Override
	public void onApplicationStartup() {

		try {
			
			Iterable<SSHInterface> interfaces = interfaceService.allObjects();
			
			for(SSHInterface iface : interfaces) {
				
				SSHInterfaceFactory factory = appContext.getBean(iface.getInterfaceFactory());
				addInterface(iface.getAddressToBind(),  iface.getPortToBind(), 
						new SSHDInterface(factory, iface));
			}
			
			
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
		
		sshContext.getPolicy(FileSystemPolicy.class).setFileFactory(new FileFactory() {

			@Override
			public AbstractFileFactory<?> getFileFactory(SshConnection con) {
				return SSHDServiceImpl.this.getFileFactory(userService.getUser(con.getUsername()));
			}
			
		});
	}
	
	@Override
	protected void configureForwarding(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		ForwardingPolicy policy = new ForwardingPolicy() {
			public boolean checkInterfacePermitted(SshConnection con, String originHost, int originPort) {
				/**
				 * We do not allow remote forwarding
				 */
				return false;
			}
			
			public boolean checkHostPermitted(SshConnection con, String host, int port) {
				return con.getUsername().equals("admin");
			}
		};
		policy.allowForwarding();

		sshContext.setPolicy(ForwardingPolicy.class, policy);
	}
	
	@Override
	public SshServerContext createContext(SshEngineContext daemonContext, SocketChannel sc)
			throws IOException, SshException {
		return createServerContext(daemonContext, sc);
	}
	
	@Override
	public AbstractFileFactory<?> getFileFactory(User user) {
		
		permissionService.setupUserContext(user);
		
//		SSHDConfiguration sshdConfig = configService.getObject(SSHDConfiguration.class);
		
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
					if(mount.hasHome()) {
						home = mount.getHomeMount(user);
					}
				}
			}
			
			if(Objects.isNull(home)) {
//				if(sshdConfig.getHomeDirectoryMaxSpace() > 0) {
//					home = new VirtualMountTemplate("/",
//							"home/" + user.getUsername(), 
//							new SpaceRestrictedFileFactoryAdapter(new VFSFileFactory(),
//									sshdConfig.getHomeDirectoryMaxSpace()), 
//							true);
//				} else {
					home = new VirtualMountTemplate("/",
							"home/" + user.getUsername(), 
							new VFSFileFactory(), 
							true);
//				}
			}

			return new VirtualFileFactory(home, 
					mounts.toArray(new VirtualMountTemplate[0]));
			
		} catch (IOException | PermissionDeniedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			permissionService.clearUserContext();
		}
	}
	
	@Override
	public void applyConfiguration(SshServerContext sshContext) {
		
		SSHDConfiguration sshdConfig = configService.getObject(SSHDConfiguration.class);
		
		sshContext.setIdleConnectionTimeoutSeconds(sshdConfig.getIdleConnectionTimeoutSecs());
				
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMaxPacketSize(sshdConfig.getSftpMaximumPacketSize());
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMaxWindowSize(sshdConfig.getSftpMaximumWindowSpace());
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMinWindowSize(sshdConfig.getSftpMinimumWindowSpace());
		sshContext.getPolicy(FileSystemPolicy.class).setSFTPCharsetEncoding(sshdConfig.getSftpCharacterSetEncoding());

		if(sshdConfig.getEnableSCP()) {
			sshContext.getChannelFactory().supportedCommands().add("scp", ScpCommand.class);
		}
	}
	
	protected void configureChannels(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		SSHDConfiguration sshdConfig = configService.getObject(SSHDConfiguration.class);
		
		sshContext.setIdleConnectionTimeoutSeconds(sshdConfig.getIdleConnectionTimeoutSecs());
				
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMaxPacketSize(sshdConfig.getSftpMaximumPacketSize());
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMaxWindowSize(sshdConfig.getSftpMaximumWindowSpace());
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMinWindowSize(sshdConfig.getSftpMinimumWindowSpace());
		sshContext.getPolicy(FileSystemPolicy.class).setSFTPCharsetEncoding(sshdConfig.getSftpCharacterSetEncoding());

		if(sshdConfig.getEnableSCP()) {
			sshContext.getChannelFactory().supportedCommands().add("scp", ScpCommand.class);
		}
		
		sshContext.setChannelFactory(new VirtualChannelFactory() {

			@Override
			protected ChannelNG<SshServerContext> createSessionChannel(SshConnection con)
					throws UnsupportedChannelException, PermissionDeniedException {
				
				ShellCommandFactory scf = new ShellCommandFactory(
						new FileSystemCommandFactory(),
						new SshClientsCommandFactory(),
						userCommands);
				
				permissionService.setupUserContext(userService.getUser(con.getUsername()));
				
				try {
					
					for(PluginCommandFactory cf : appContext.getBeans(PluginCommandFactory.class)) {
						try {
							scf.installFactory(cf.buildFactory());
						} catch(AccessDeniedException e) { }
					}
					
					VirtualShell shell = new VirtualShell(con, scf);
					
					shell.addProtectedEnvironmentVar("USER");
					shell.addProtectedEnvironmentVar("TENANT_NAME");
					shell.addProtectedEnvironmentVar("TENANT_UUID");
					
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
		
		if(updateManager.hasPendingUpdates()) {
			out.append("\n");
			out.append("There are application updates. To install type 'updates -i'\n");
		}
		
		sshContext.getPolicy(VirtualSessionPolicy.class).setWelcomeText(out.toString());
		ClassLoader classLoader = getClass().getClassLoader();
		sshContext.getPolicy(ClassLoaderPolicy.class).setClassLoader(classLoader);
	}

	@Override
	public void addInterface(SSHInterface sshInterface) throws IOException {
		addInterface(sshInterface.getAddressToBind(), 
				sshInterface.getPortToBind(), 
				getDefaultContextFactory());
		interfaces.put(sshInterface.getInterface(), 
				sshInterface);
	}

	@Override
	public boolean isListening(String intf) {
		return interfaces.containsKey(intf);
	}

	@Override
	public SSHInterface getInterface(String intf) {
		return interfaces.get(intf);
	}

	@Override
	public void removeInterface(SSHInterface sshInterface) throws UnknownHostException {
		removeInterface(sshInterface.getAddressToBind(), sshInterface.getPortToBind());
		interfaces.remove(sshInterface.getInterface());
	}
}

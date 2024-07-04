package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.ApplicationService;
import com.jadaptive.api.app.ApplicationVersion;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.db.SingletonObjectDatabase;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.i18n.I18nService;
import com.jadaptive.api.permissions.AccessDeniedException;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.session.Session;
import com.jadaptive.api.session.SessionService;
import com.jadaptive.api.session.SessionType;
import com.jadaptive.api.tenant.Tenant;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.jadaptive.plugins.sshd.commands.UserCommandFactory;
import com.sshtools.common.auth.PasswordAuthenticationProvider;
import com.sshtools.common.events.EventCodes;
import com.sshtools.common.events.EventServiceImplementation;
import com.sshtools.common.files.AbstractFileFactory;
import com.sshtools.common.files.vfs.VFSFileFactory;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.files.vfs.VirtualMountTemplate;
import com.sshtools.common.forwarding.ForwardingPolicy;
import com.sshtools.common.permissions.IPPolicy;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.ClassLoaderPolicy;
import com.sshtools.common.policy.FileFactory;
import com.sshtools.common.policy.FileSystemPolicy;
import com.sshtools.common.scp.ScpCommand;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.UnsupportedChannelException;
import com.sshtools.common.util.UnsignedInteger32;
import com.sshtools.server.LoadBalancerPolicy;
import com.sshtools.server.SshServer;
import com.sshtools.server.SshServerContext;
import com.sshtools.server.vsession.ShellCommandFactory;
import com.sshtools.server.vsession.VirtualChannelFactory;
import com.sshtools.server.vsession.VirtualSessionPolicy;
import com.sshtools.server.vsession.commands.fs.FileSystemCommandFactory;
import com.sshtools.synergy.nio.ProtocolContextFactory;
import com.sshtools.synergy.nio.SshEngineContext;
import com.sshtools.synergy.ssh.ChannelNG;
import com.sshtools.vsession.commands.ssh.SshClientsCommandFactory;


@Service
public class SSHDServiceImpl extends SshServer implements SSHDService, StartupAware {

	private static Logger LOG = LoggerFactory.getLogger(SSHDServiceImpl.class);

	public static final String SSH_SERVER = "SSH Server";
	
	@Autowired
	private ApplicationService appContext; 
	
	@Autowired
	private UserCommandFactory userCommands; 
	
	@Autowired
	private PermissionService permissionService; 

	@Autowired
	private UserService userService; 
	
	@Autowired
	private SingletonObjectDatabase<SSHDConfiguration> configService;
	
	@Autowired
	private SSHInterfaceService interfaceService; 
	
	@Autowired
	private I18nService i18n;
	
	@Autowired
	private EventService eventService; 
	
	@Autowired
	private TenantService tenantService; 
	
	@Autowired
	private SessionService sessionService; 
	
	
	Map<String,SSHInterface> interfaces = new HashMap<>();
	
	IPPolicy ipPolicy = new IPPolicy();
	LoadBalancerPolicy lbPolicy= new LoadBalancerPolicy();
	
	String softwareVersionComments = "JADAPTIVE";

	private boolean initialised;
	
	public SSHDServiceImpl() {
		super();

		this.fileFactory = new FileFactory() {
			@Override
			public AbstractFileFactory<?> getFileFactory(SshConnection con) throws IOException, PermissionDeniedException {
				return SSHDServiceImpl.this.getFileFactory(
								userService.getUser(tenantService.resolveUserName(con.getUsername())));
			}
			
		};
	}
	
	private void processConfigurationChanges(SSHDConfiguration sshdConfig) {
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Applying SSHD configuration to local policies security={}", sshdConfig.getSecurityLevel().name());
		}
		
		setSecurityLevel(sshdConfig.getSecurityLevel());
		
		lbPolicy.setProxyProtocolEnabled(sshdConfig.getProxyProtocolEnabled());
		
		if(lbPolicy.isProxyProtocolEnabled()) {
			for(String address: sshdConfig.getLoadBalancerIPs()) {
				lbPolicy.allowIPAddress(address);
			}
		}
		
		if(sshdConfig.getEnableBanning()) {
			ipPolicy.enableTemporaryBanning();
		} else {
			ipPolicy.disableTemporaryBanning();
		}
		
		ipPolicy.setTemporaryBanTime(sshdConfig.getTemporaryBanPeriod());
		try {
			ipPolicy.getBlacklist().reset(sshdConfig.getBlockedIPs());
		} catch (UnknownHostException e) {
			LOG.error("Invalid IP in blocked IP list", e);
		}
		try {
			ipPolicy.getWhitelist().reset(sshdConfig.getAllowedIPs());
		} catch (UnknownHostException e) {
			LOG.error("Invalid IP in allowed IP list", e);
		}
			
	}

	@Override
	public void onApplicationStartup() {

		try {
			
			eventService.updated(SSHDConfiguration.class, (evt) -> {
				processConfigurationChanges(evt.getObject());
			});
			
			processConfigurationChanges(configService.getObject(SSHDConfiguration.class));
			
			Iterable<SSHInterface> interfaces = interfaceService.allObjects();
			
			for(SSHInterface iface : interfaces) {
				
				SSHInterfaceFactory<?,?> factory = appContext.getBean(iface.getInterfaceFactory());
				addInterface(iface.getAddressToBind(),  iface.getPortToBind(), 
						new SSHDInterface<>(factory, iface));
			}
			
			EventServiceImplementation.getInstance().addListener((evt) -> {
				
				SshConnection con = (SshConnection)evt.getAttribute(EventCodes.ATTRIBUTE_CONNECTION);
				switch(evt.getId()) {
				
				case EventCodes.EVENT_AUTHENTICATION_COMPLETE:
				{
					Tenant tenant = (Tenant) con.getProperty(SSHDService.TENANT);
					User user = (User) con.getProperty(SSHDService.USER);
					if(Objects.nonNull(tenant)) {
						tenantService.executeAs(tenant, ()->{
							con.setProperty(SSHDService.SESSION, sessionService.createSession(tenant, user, 
									con.getRemoteIPAddress(), con.getRemoteIdentification(), SessionType.SSH, null));
						});
					}
					
					break;
				}
				case EventCodes.EVENT_DISCONNECTED:
				{
					Tenant tenant = (Tenant) con.getProperty(SSHDService.TENANT);
					Session session = (Session) con.getProperty(SSHDService.SESSION);
					
					if(Objects.nonNull(tenant)) {
						tenantService.executeAs(tenant, ()->{
							sessionService.closeSession(session);
						});
					}
					break;
				}
				default:
					break;
				}
			});

			if(getEngine().getContext().getListeningInterfaces().length > 0)
				start(true);
			else {
				LOG.info("No SSH interfaces configured, not starting.");
			}
		} catch (IOException e) {
			LOG.error("SSHD service failed to start", e);
		} finally {
			initialised = true;
		}
	}
	
	@Override
	public void setSoftwareVersionComments(String softwareVersionComments) {
		this.softwareVersionComments = softwareVersionComments;
	}
	
	@Override
	public Integer getStartupPosition() {
		return 0;
	}

	@Override
	protected void configureFilesystem(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		sshContext.getPolicy(FileSystemPolicy.class).setFileFactory(fileFactory);

	}
	
	@Override
	protected void configureForwarding(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		
		SSHDConfiguration sshdConfig = configService.getObject(SSHDConfiguration.class);
		
		if(!sshdConfig.getEnableLocalForwarding()) {
			ForwardingPolicy policy = new ForwardingPolicy();
			policy.denyForwarding();
			sshContext.setPolicy(ForwardingPolicy.class, policy);
		} else {
			ForwardingPolicy policy = new ForwardingPolicy() {
				public boolean checkInterfacePermitted(SshConnection con, String originHost, int originPort) {
					/**
					 * We do not allow remote forwarding
					 */
					return false;
				}
				
				public boolean checkHostPermitted(SshConnection con, String host, int port) {
					SSHDConfiguration sshdConfig = configService.getObject(SSHDConfiguration.class);
					return sshdConfig.getAllowedForwarding().contains(host) 
							|| sshdConfig.getAllowedForwarding().contains(host + ":" + port);
				}
			};
			
			policy.allowForwarding();
	
			sshContext.setPolicy(ForwardingPolicy.class, policy);
		}

	}
	
	@Override
	public SshServerContext createContext(SshEngineContext daemonContext, SocketChannel sc)
			throws IOException, SshException {
		return createServerContext(daemonContext, sc);
	}
	
	@Override
	public AbstractFileFactory<?> getFileFactory(User user) {
		
		permissionService.setupUserContext(user);
		
		try {
			List<VirtualMountTemplate> mounts = new ArrayList<>();
			VirtualMountTemplate home = null;
			for(PluginFileSystemMount mount : appContext.getBeans(PluginFileSystemMount.class)) {
				mounts.addAll(mount.getAdditionalMounts());
				if(mount.hasHome()) {
					if(Objects.nonNull(home)) {
						if(LOG.isWarnEnabled()) {
							LOG.warn("A plugin attempted to configure a home mount but it was already defined.");
						}
						continue;
					}
					if(mount.hasHome()) {
						home = mount.getHomeMount(user);
					}
				}
			}
			
			if(Objects.isNull(home)) {
				    String uri = "home/" + user.getUsername();
					home = new VirtualMountTemplate("/",
							uri, 
							new VFSFileFactory(uri), 
							true);
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
	public void applyConfiguration(SshServerContext sshContext, PasswordAuthenticationProvider passwordProvider) {
		
		SSHDConfiguration sshdConfig = configService.getObject(SSHDConfiguration.class);
		
		sshContext.setIdleConnectionTimeoutSeconds(sshdConfig.getIdleConnectionTimeoutSecs());
				
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMaxPacketSize(sshdConfig.getSftpMaximumPacketSize());
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMaxWindowSize(new UnsignedInteger32(sshdConfig.getSftpMaximumWindowSpace()));
		sshContext.getPolicy(FileSystemPolicy.class).setSftpMinWindowSize(new UnsignedInteger32(sshdConfig.getSftpMinimumWindowSpace()));
		sshContext.getPolicy(FileSystemPolicy.class).setSFTPCharsetEncoding(sshdConfig.getSftpCharacterSetEncoding());

		sshContext.setPolicy(IPPolicy.class, ipPolicy);
		sshContext.setPolicy(LoadBalancerPolicy.class, lbPolicy);
		
		if(sshdConfig.getEnableSCP()) {
			sshContext.getChannelFactory().supportedCommands().add(new ScpCommand.ScpCommandFactory());
		}
		
		if(sshdConfig.getEnablePassword()) {
			sshContext.getAuthenticationMechanismFactory().addProvider(passwordProvider);
		}
		
		sshContext.setSocketOptionKeepAlive(sshdConfig.getKeepAlive());
		sshContext.setSocketOptionTcpNoDelay(sshdConfig.isTcpNoDelay());
		sshContext.setReceiveBufferSize(sshdConfig.getReceiveBufferSize());
		sshContext.setSendBufferSize(sshdConfig.getSendBufferSize());
		
		sshContext.setSoftwareVersionComments(softwareVersionComments);
	}
	
	protected void configureChannels(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		sshContext.setChannelFactory(new VirtualChannelFactory() {

			@Override
			protected ChannelNG<SshServerContext> createSessionChannel(SshConnection con)
					throws UnsupportedChannelException, PermissionDeniedException {
				
				ShellCommandFactory scf = new ShellCommandFactory(
						new FileSystemCommandFactory(),
						new SshClientsCommandFactory(),
						userCommands);
				
				permissionService.setupUserContext(userService.getUser(tenantService.resolveUserName(con.getUsername())));
				
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
					
					appContext.autowire(shell);
					
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
		out.append(i18n.format("vendor", Locale.getDefault(), "product.name"));
		out.append("\n");
		out.append(ApplicationVersion.getVersion());
		out.append("\n==============================================\n");
		out.append("\nType 'help' for a list of commands.\n");
		
//		if(updateManager.hasPendingUpdates()) {
//			out.append("\n");
//			out.append("There are application updates. To install type 'updates -i'\n");
//		}
		
		sshContext.getPolicy(VirtualSessionPolicy.class).setWelcomeText(out.toString());
		ClassLoader classLoader = getClass().getClassLoader();
		sshContext.getPolicy(ClassLoaderPolicy.class).setClassLoader(classLoader);
	}

	@Override
	public void addInterface(SSHInterface iface) throws IOException {
		SSHInterfaceFactory<?,?> factory = appContext.getBean(iface.getInterfaceFactory());
		addInterface(iface.getAddressToBind(),  iface.getPortToBind(), new SSHDInterface<>(factory, iface));
	}

	@Override
	public void addInterface(String addressToBind, int portToBind) throws IOException {
		super.addInterface(addressToBind, portToBind);
		if(initialised && !getEngine().isStarted()) {
			getEngine().startup();
		}
	}

	@Override
	public void addInterface(String addressToBind, int portToBind, ProtocolContextFactory<?> contextFactory)
			throws IOException {
		super.addInterface(addressToBind, portToBind, contextFactory);
		if(initialised && !getEngine().isStarted()) {
			getEngine().startup();
		}
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

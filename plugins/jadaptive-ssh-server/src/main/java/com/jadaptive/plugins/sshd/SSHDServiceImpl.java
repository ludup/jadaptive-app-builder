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

import com.jadaptive.api.app.App;
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
import com.sshtools.common.forwarding.ForwardingPolicy.ForwardingPolicyBuilder;
import com.sshtools.common.forwarding.ForwardingRequest.ForwardingType;
import com.sshtools.common.permissions.IPPolicy;
import com.sshtools.common.permissions.IPPolicy.IPPolicyBuilder;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.ClassLoaderPolicy.ClassLoaderPolicyBuilder;
import com.sshtools.common.policy.FileFactory;
import com.sshtools.common.policy.FileSystemPolicy;
import com.sshtools.common.policy.FileSystemPolicy.FileSystemPolicyBuilder;
import com.sshtools.common.scp.ScpCommand;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.UnsupportedChannelException;
import com.sshtools.server.LoadBalancerPolicy;
import com.sshtools.server.LoadBalancerPolicy.LoadBalancerPolicyBuilder;
import com.sshtools.server.SshServer;
import com.sshtools.server.SshServerContext;
import com.sshtools.server.vsession.ShellCommandFactory;
import com.sshtools.server.vsession.VirtualChannelFactory;
import com.sshtools.server.vsession.VirtualSessionPolicy.VirtualSessionPolicyBuilder;
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
	private App appContext; 
	
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
	
	
	private Map<String,SSHInterface> interfaces = new HashMap<>();
	private String softwareVersionComments = "JADAPTIVE";
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
	
	private void processConfigurationChanges(SSHDConfiguration sshdConfig) throws UnknownHostException {
		
		if(LOG.isInfoEnabled()) {
			LOG.info("Applying SSHD configuration to local policies security={}", sshdConfig.getSecurityLevel().name());
		}
		
		setSecurityLevel(sshdConfig.getSecurityLevel());
		
		@SuppressWarnings("unused")
		var lbPolicy = createLbPolicy(sshdConfig);
		// TODO set it
		
		
		@SuppressWarnings("unused")
		var ipPolicy= createIpPolicy(sshdConfig);

		// TODO set it
			
	}

	private IPPolicy createIpPolicy(SSHDConfiguration sshdConfig) {
		try {
			return IPPolicyBuilder.create().
					withTemporaryBanTime(sshdConfig.getTemporaryBanPeriod()).
					withTemporaryBanning(sshdConfig.getEnableBanning()).
					withAllowedIPAddresses(sshdConfig.getAllowedIPs()).
					withBlockedIPAddresses(sshdConfig.getBlockedIPs()).
					build();
		}
		catch(UnknownHostException uhe) {
			throw new IllegalArgumentException("Invalid IP address in allow or block list.", uhe);
		}
	}

	private LoadBalancerPolicy createLbPolicy(SSHDConfiguration sshdConfig) {
		return LoadBalancerPolicyBuilder.create().
			withProxyProtocol(sshdConfig.getProxyProtocolEnabled()).
			withSupportedIPAddresses(sshdConfig.getLoadBalancerIPs()).
			build();
	}

	@Override
	public void onApplicationStartup() {

		try {
			
			eventService.updated(SSHDConfiguration.class, (evt) -> {
				try {
					processConfigurationChanges(evt.getObject());
				} catch (UnknownHostException e) {
					LOG.error("Failed to apply updated SSHD configuration.", e);
				}
			});
			
			processConfigurationChanges(configService.getObject(SSHDConfiguration.class));
			
			Iterable<SSHInterface> interfaces = interfaceService.allObjects();
			
			for(SSHInterface iface : interfaces) {
				
				SSHInterfaceFactory<?,?> factory = appContext.getBean(iface.getInterfaceFactory());
				addInterface(iface.getAddressToBind(),  iface.getActualPortToBind(), 
						new SSHDInterface<>(factory, iface));
			}
			
//			EventServiceImplementation.getInstance().addListener((evt) -> {
//				
//				SshConnection con = (SshConnection)evt.getAttribute(EventCodes.ATTRIBUTE_CONNECTION);
//				switch(evt.getId()) {
//				
//				case EventCodes.EVENT_AUTHENTICATION_COMPLETE:
//				{
//					Tenant tenant = (Tenant) con.getProperty(SSHDService.TENANT);
//					User user = (User) con.getProperty(SSHDService.USER);
//					if(Objects.nonNull(tenant)) {
//						tenantService.executeAs(tenant, ()->{
//							con.setProperty(SSHDService.SESSION, sessionService.createSession(tenant, user, 
//									con.getRemoteIPAddress(), con.getRemoteIdentification(), SessionType.SSH, null));
//						});
//					}
//					
//					break;
//				}
//				case EventCodes.EVENT_DISCONNECTED:
//				{
//					Tenant tenant = (Tenant) con.getProperty(SSHDService.TENANT);
//					Session session = (Session) con.getProperty(SSHDService.SESSION);
//					
//					if(Objects.nonNull(tenant)) {
//						tenantService.executeAs(tenant, ()->{
//							sessionService.closeSession(session);
//						});
//					}
//					break;
//				}
//				default:
//					break;
//				}
//			});
		} catch (IOException e) {
			LOG.error("SSHD service failed to start", e);
		}
	}
	
	@Override
	public void onAfterApplicationStartup() {

		try {

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
	
	@SuppressWarnings("unused")
	@Override
	protected void configureForwarding(SshServerContext sshContext, SocketChannel sc) throws IOException, SshException {
		
		
		SSHDConfiguration sshdConfig = configService.getObject(SSHDConfiguration.class);
		
		if(!sshdConfig.getEnableLocalForwarding()) {
			sshContext.setPolicy(ForwardingPolicyBuilder.create().build());
		} else {
			sshContext.setPolicy(ForwardingPolicyBuilder.create().
					allowTCPForwarding().
					withValidator((type,request,role) -> {
						if(type == ForwardingType.REMOTE) {
							/**
							 * We do not allow remote forwarding
							 */
							return false;
						}
						else {
							SSHDConfiguration newSshdConfig = configService.getObject(SSHDConfiguration.class);
							return newSshdConfig.getAllowedForwarding().contains(request.destinationAddress()) 
									|| newSshdConfig.getAllowedForwarding().contains(request.destinationAddress() + ":" + request.destinationPort());
						}
					}).
					build());
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
		
		sshContext.setPolicy(FileSystemPolicyBuilder.create().
				withSftpMaxPacketSize(sshdConfig.getSftpMaximumPacketSize()).
				withSftpMaxWindowSize(sshdConfig.getSftpMaximumWindowSpace()).
				withSftpMinWindowSize(sshdConfig.getSftpMinimumWindowSpace()).
				withCharsetEncoding(sshdConfig.getSftpCharacterSetEncoding()).
				build());

		sshContext.setPolicy(createLbPolicy(sshdConfig));
		sshContext.setPolicy(createIpPolicy(sshdConfig));
		
		if(sshdConfig.getEnableSCP()) {
			sshContext.getChannelFactory().supportedCommands().add(new ScpCommand.ScpCommandFactory());
		}
		
		if(sshdConfig.getEnablePassword() && passwordProvider != null) {
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
		
		sshContext.setPolicy(VirtualSessionPolicyBuilder.create().
				withWelcomeText(out.toString()).
				build());
		
		sshContext.setPolicy(ClassLoaderPolicyBuilder.create().
				withClassLoader(getClass().getClassLoader()).
				build());
	}

	@Override
	public void addInterface(SSHInterface iface) throws IOException {
		SSHInterfaceFactory<?,?> factory = appContext.getBean(iface.getInterfaceFactory());
		addInterface(iface.getAddressToBind(),  iface.getActualPortToBind(), new SSHDInterface<>(factory, iface));
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
		removeInterface(sshInterface.getAddressToBind(), sshInterface.getActualPortToBind());
		interfaces.remove(sshInterface.getInterface());
	}

}

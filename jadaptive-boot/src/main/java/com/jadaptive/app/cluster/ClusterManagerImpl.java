package com.jadaptive.app.cluster;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jgroups.Address;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadaptive.api.app.App;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.ApplicationServiceImpl;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.app.VersionProvider;
import com.jadaptive.api.cluster.BroadcastableEvent;
import com.jadaptive.api.cluster.ClusterEvent;
import com.jadaptive.api.cluster.ClusterManager;
import com.jadaptive.api.cluster.ClusterNode;
import com.jadaptive.api.cluster.ClusterNode.ClusterNodeStatus;
import com.jadaptive.api.cluster.ClusterNodeConnectedEvent;
import com.jadaptive.api.cluster.ClusterNodeDisconnectedEvent;
import com.jadaptive.api.cluster.ClusterService;
import com.jadaptive.api.cluster.ClusterServiceProvider;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SystemOnlyObjectDatabase;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.entity.ObjectNotFoundException;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.http.HttpHelpers;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.ui.UriRedirect;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.gardensched.DistributedScheduledExecutor;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class ClusterManagerImpl extends AbstractUUIDObjectServceImpl<ClusterNode> implements ClusterManager, StartupAware {

	private static Logger LOG = LoggerFactory.getLogger(ClusterManagerImpl.class);
	
	@Autowired
	private SystemOnlyObjectDatabase<ClusterNode> clusterNodes;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EventService eventService;

	@Autowired
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private App app;

	private String serverId;
	private String configuredHostname;
	private String hostname;

	@Override
	public void initCluster() {
		configuredHostname = hostname = ApplicationProperties.getValue("ha.hostname", "");
		if (hostname.equals("")) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				hostname = "localhost";
			}	
			LOG.warn("ha.hostname is not set, using a generated ha.hostname of " + hostname);
		}
		
			
		serverId = ApplicationProperties.getValue("ha.id","");
		if (serverId.equals("")) {
			serverId = UUID.nameUUIDFromBytes((hostname + ":"+ getPort()).getBytes()).toString();
			LOG.warn("ha.id is not set, using a generated ha.id of " + serverId);
		} 
	
		ClusterNode thisNode;
		try {
			thisNode = getObjectByUUID(serverId);
		}
		catch(ObjectNotFoundException onfe) {
			thisNode = new ClusterNode();
			thisNode.setUuid(serverId);
		}
		
		if(StringUtils.isBlank(thisNode.getAuthenticationToken())) {
			thisNode.setAuthenticationToken(UUID.randomUUID().toString());
		}
		ClusterAuth.setup(thisNode.getAuthenticationToken(), serverId);
		
		thisNode.setHostname(hostname);			
		try {
			VersionProvider vp = ApplicationServiceImpl.getInstance().getBean(VersionProvider.class);
			thisNode.setVersion(Optional.ofNullable(
				vp).map(VersionProvider::getVersion).
				orElse("Unknown"));
		}
		catch(Exception e) {
			thisNode.setVersion("Unknown");
		}
		thisNode.setTimeZone(TimeZone.getDefault().getID());
		thisNode.setStatus(ClusterNodeStatus.UNAUTHORIZED);
		thisNode.setGroupAddress(null);
		thisNode.setServices(Arrays.asList(new ClusterService(ClusterService.HTTPS_SERVICE, getPort())));
		
		saveOrUpdate(thisNode);
		
		eventService.deleting(ClusterNode.class, evt -> {
			if(evt.getObject().getStatus() == ClusterNodeStatus.ONLINE) {
				throw new IllegalStateException("Cannot delete nodes that are ONLINE");
			}
		});
	}

	@Override
	public void onAfterApplicationStartup() {
		tenantService.asSystem(() -> {
			var thisNode = getThisNode();
			thisNode.setServices(getServices().stream().toList());
			saveOrUpdate(thisNode);
		});
	}

	@Override
	public void onApplicationStartup() {
	}

	@Override
	public void setupCluster(DistributedScheduledExecutor executor) {
		
		setupEventsProxy(executor);
			
		var currentMembers = executor.view().getMembers().stream().map(Address::toString).toList();
		for(var node : clusterNodes.list(ClusterNode.class)) {
			if(ClusterNodeStatus.ONLINE.equals(node.getStatus()) && 
				!node.getUuid().equals(serverId) && 
				!currentMembers.contains(node.getGroupAddress())) {
				nodeStatusChanged(node, node.getStatus(), ClusterNodeStatus.OFFLINE);
			}
		}
		
		executor.addListener((leftMembers, joinedMembers) -> {
			for(var left : leftMembers) {
				LOG.info("{} left the cluster", left);
				try {
					nodeStatusChanged(clusterNodeForAddress(left.toString()), ClusterNodeStatus.ONLINE, ClusterNodeStatus.OFFLINE);
				}
				catch(Exception e) {
					//
				}
			}
			
			for(var joined : joinedMembers) {
				LOG.info("{} joined the cluster", joined);
				try {
					nodeStatusChanged(clusterNodeForAddress(joined.toString()), ClusterNodeStatus.OFFLINE, ClusterNodeStatus.ONLINE);
				}
				catch(Exception e) {
					//
				}
			}
		});
		
		ClusterNode thisNode = getObjectByUUID(serverId);
		thisNode.setStatus(ClusterNodeStatus.ONLINE);
		thisNode.setGroupAddress(executor.address().toString());
		saveOrUpdate(thisNode);
	}
	
	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public Optional<String> getExternalHostname() {
		var hostname = ApplicationProperties.getValue("ha.externalHostname", configuredHostname);
		return hostname.equals("") ? Optional.empty() : Optional.of(hostname);
	}

	@Override
	public Set<ClusterService> getServices() {
		
		Set<ClusterService> services = new LinkedHashSet<ClusterService>();
		for(var bean : app.getBeans(ClusterServiceProvider.class)) {
			services = bean.transform(services);
		}
		if(services == null)
			services = new LinkedHashSet<>();

		var port = getPort();
		services.add(new ClusterService(ClusterService.HTTPS_SERVICE, port));
		return services;
	}
	
	public ClusterNode clusterNodeForAddress(String groupAddress) {
		return clusterNodes.searchObjects(ClusterNode.class,
				SearchField.eq("groupAddress", groupAddress)
			).stream().findFirst().orElseThrow(() -> new ObjectNotFoundException(groupAddress));
	}

	@Override
	public Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate) {

		var node = getObjectByUUID(obj.getUuid());
		if (column.equals("uuid")) {
			var row = Html.div();
			var spn = Html.span(obj.getUuid());
			if(obj.getUuid().equals(getServerId())) {
				spn.addClass("fw-bolder");
			}
			row.appendChild(spn);
			
			var div = Html.div("mt-2", "ms-2", "text-muted");
			
			var crow = Html.div("row");
			crow.appendChild(Html.div("col-4").appendChild(Html.em(Html.i18n(ClusterNode.RESOURCE_KEY, "version.name"))));
			crow.appendChild(Html.div("col-8").text(node.getVersion()));
			div.appendChild(crow);
			
			crow = Html.div("row");
			crow.appendChild(Html.div("col-4").appendChild(Html.em(Html.i18n(ClusterNode.RESOURCE_KEY, "groupAddress.name"))));
			crow.appendChild(Html.div("col-8").text(node.getGroupAddress()));
			div.appendChild(crow);
			
			crow = Html.div("row");
			crow.appendChild(Html.div("col-4").appendChild(Html.em(Html.i18n(ClusterNode.RESOURCE_KEY, "timeZone.name"))));
			crow.appendChild(Html.div("col-8").text(node.getTimeZone()));
			div.appendChild(crow);
			
			row.appendChild(div);
			
			return row;
		}
		else {
			if (column.equals("hostname")) {
				var row = Html.div();
				var addr = Html.span(node.getHostname());
				if(obj.getUuid().equals(getServerId())) {
					addr.addClass("fw-bolder");
				}
				row.appendChild(addr);
				if(node.getServices().size() > 0) {
					var div = Html.div("mt-2", "ms-2", "text-muted");
					node.getServices().forEach(s -> {
						var srvcol = Html.div("col-8");
						srvcol.appendChild(Html.em(s.getService()));
						var portcol = Html.div("col-4");
						portcol.text(String.valueOf(s.getPort()));
	
						var crow = Html.div("row");
						crow.appendChild(srvcol);
						crow.appendChild(portcol);
						
						div.appendChild(crow);
						
					});
					row.appendChild(div);
				}
				return row;
			}
			else if (column.equals("status")) {
				var row = Html.span();
				if(node.getUuid().equals(getServerId())) {
					row.addClass("fw-bolder");
				}
				switch(node.getStatus()) {
				case ONLINE:
					row.appendChild(Html.i("fa-solid", "fa-circle-check","text-success"));
					break;
				case UNAUTHORIZED:
					row.appendChild(Html.i("fa-solid", "fa-triangle-exclamation","text-warning"));
					break;
				default:
					row.appendChild(Html.i("fa-solid", "fa-circle-exclamation","text-danger"));
					break;
				}
				row.appendChild(Html.i18n(ClusterNode.RESOURCE_KEY, "clusterNode."+ node.getStatus().name()).addClass("ms-3"));
				return row;
			}
		}
		return Html.span("");
	}

	private void setupEventsProxy(DistributedScheduledExecutor executor) {
		eventService.registerListener(evt -> {
			if(evt instanceof SystemEvent sysevt && evt instanceof BroadcastableEvent && !sysevt.isRemote()) {
				
				/* TODO Really should be an assertion, its mainly to highlight to developers
				 */
				try {
					evt.getClass().getConstructor();
				}
				catch(NoSuchMethodError msme) {
					LOG.error("{} events tagged with {} must have an emmpty constructor.", SystemEvent.class.getName(), BroadcastableEvent.class.getName(), msme);
					throw msme;
				}
				catch(Exception e) {}
				
				/* Who are we firing event as? */
				String uuuid;
				try {
					uuuid = permissionService.getCurrentUser().getUuid();
				}
				catch(Exception e) {
					uuuid = permissionService.getSystemUser().getUuid();
				}
				var fuuuid = uuuid;
				var tenant = tenantService.getCurrentTenant().getUuid();
				
				/* Put the actual save on the queue, we don't want to hold up normal local
				 * event listeners for synchronous events
				 */
				/* Send event */
				LOG.info("Sending event {} to cluster as {} @ {}", 
						sysevt.getResourceKey(),
						fuuuid,
						tenant);
				var cevt = new ClusterEvent(
						sysevt, 
						serverId, 
						tenant,
						fuuuid);
				cevt.setUuid(sysevt.getUuid());
				executor.event(cevt);
				
			}
		});
		
		executor.addBroadcastListener((sndr, evt) -> {
			var cevt = (ClusterEvent)evt;
			if(!sndr.equals(executor.address())) {
				LOG.info("Received broadcast event {} as tenant {} and user {}", cevt.getEvent().getResourceKey(), cevt.getTenant(), cevt.getUser());
				
				/* Re-fire, in context of original tenant and user */
				tenantService.executeAs(tenantService.getTenantByUUID(cevt.getTenant()), () -> {
					User usr = permissionService.getSystemUser();
					if(!cevt.getUser().equals(usr.getUuid())) {
						usr = userService.getObjectByUUID(cevt.getUser());
					}
					
					/* Set to remote so it is not re-broadcast or audited */
					cevt.getEvent().setRemote(true);
					
					permissionService.as(usr, () -> {
						eventService.publishEvent(cevt.getEvent());
						return null;
					});
				});
			}
		});

	}

	private void nodeStatusChanged(ClusterNode document, ClusterNodeStatus lastStatus,
			ClusterNodeStatus newStatus) {
		LOG.info("Server with ID of {} status changed from {} to {}.", document.getUuid(), lastStatus, newStatus);

		try {
			tenantService.asSystem(() -> {
				document.setStatus(newStatus);
				saveOrUpdate(document);
			});
		}
		finally {
		
			if(!document.getUuid().equals(serverId)) {
				if(lastStatus == ClusterNodeStatus.OFFLINE) {
					eventService.publishEvent(new ClusterNodeConnectedEvent(document));
				}
	
				if(newStatus == ClusterNodeStatus.OFFLINE) {
					eventService.publishEvent(new ClusterNodeDisconnectedEvent(document));
				}
			}
		}

		
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public String getServerId() {
		return serverId;
	}

	@Override
	protected Class<ClusterNode> getResourceClass() {
		return ClusterNode.class;
	}

	private int getPort() {
		return Integer.parseInt(
			ApplicationProperties.getValue("ha.port", 
				ApplicationProperties.getValue("server.port", "443")
			)
		);
	}

	@Override
	public ClusterNode getThisNode() {
		return getObjectByUUID(serverId);
	}

	@Override
	public ClusterNode getObjectByGroupName(String groupName) {
		return clusterNodes.searchObjects(ClusterNode.class, SearchField.eq("groupName", groupName)).stream().findFirst().orElseThrow(() -> new ObjectNotFoundException(groupName));
	}

	@Override
	public boolean isJoined() {
		return ApplicationProperties.getValue("ha.clusterName", "").length() > 0;
	}
	@Override
	public void join(String token, String refreshToken, String url, boolean insecureSsl) throws IOException, InterruptedException {
		
		LOG.info("Obtaining cluster configuration.");
	
		var actionUri = url +"/oauth2/join-cluster";
		LOG.info("Using token to obtain cluster configuration. {}", actionUri);
		
		var request = HttpRequest.newBuilder()
				  .uri(URI.create(actionUri))
				  .headers("Authentication", "Bearer " + token)
				  .GET()
				  .build();
		
		var bldr = HttpClient.newBuilder();
		if(insecureSsl) {
			LOG.warn("Ignoring SSL errors.");
			bldr.sslContext(HttpHelpers.insecureContext());
		}
		
		var response = bldr
				  .build()
				  .send(request, BodyHandlers.ofString());
		
		if(response.statusCode() != HttpServletResponse.SC_OK) {
			throw new IOException("Unexpected status code " + response.statusCode());
		}
		
		var body = response.body();
		var clusterResponseObj = objectMapper.readValue(body, ClusterInfoResponse.class);
		var error = clusterResponseObj.getError(); 
		if(isNotBlank(error)) {
			if(isNotBlank(clusterResponseObj.getErrorDescription()))
				throw new IOException("Failed to get token, error '" + error + ". " + clusterResponseObj.getErrorDescription());
			else
				throw new IOException("Failed to get token, error '" + error + ". ");
		}
		
		LOG.info("Updating local configuration .. ");
		
		var clusterJoin = clusterResponseObj.getBody();

		// TODO the joined node should also be normalized with the below upon the first join of another node
		
		
		var confd = Paths.get("conf.d");

		/* Database config */
		var databasePropertiesFile = confd.resolve("database.properties");
		var databaseProperties = new Properties();
		if(Files.exists(databasePropertiesFile)) {
			try(var rdr = Files.newBufferedReader(databasePropertiesFile)) {
				databaseProperties.load(rdr);
			}
		}
		
		databaseProperties.setProperty("mongodb.connection", clusterJoin.mongoDbUrl());
		databaseProperties.setProperty("mongodb.embedded", "false");
		databaseProperties.remove("mongodb.hostname");
		databaseProperties.remove("mongodb.port");
		
		LOG.info("Saving new database configuration .. ");

		try(var rdr = Files.newBufferedWriter(databasePropertiesFile)) {
			databaseProperties.store(rdr, "Properties updated as result of cluster join");
		}
		
		/* Install4j config (or updates might change database) */
		var install4jPropertiesFile = Paths.get(".install4j").resolve("response.varfile");
		var install4jProperties = new Properties();
		if(Files.exists(install4jPropertiesFile)) {
			try(var rdr = Files.newBufferedReader(install4jPropertiesFile)) {
				install4jProperties.load(rdr);
			}
			install4jProperties.put("mongoConnectionString", clusterJoin.mongoDbUrl());
			try(var rdr = Files.newBufferedWriter(install4jPropertiesFile)) {
				install4jProperties.store(rdr, "Properties updated as result of cluster join");
			}
		}
		
		/* Cluster config */
		var clusterPropertiesFile = confd.resolve("cluster.properties");
		var clusterProperties = new Properties();
		if(Files.exists(clusterPropertiesFile)) {
			try(var rdr = Files.newBufferedReader(clusterPropertiesFile)) {
				clusterProperties.load(rdr);
			}
		}
		clusterProperties.setProperty("ha.clusterName", clusterJoin.clusterName()); 
		clusterProperties.setProperty("ha.props", clusterJoin.props());
		clusterProperties.setProperty("ha.id", getServerId());

		/* Local key config */
		if(StringUtils.isNotBlank(clusterJoin.publicKey()) && StringUtils.isNotBlank(clusterJoin.privateKey())) {
			
			LOG.info("Saving new local keys .. ");
			
			var privateFolder = Paths.get(ApplicationProperties.getValue("private.dir", "conf")).
					resolve(ApplicationProperties.getValue("private.conf", "private"));

			var prvFile = privateFolder.resolve(ApplicationProperties.getValue("private.filename", "secrets"));
			var pubFile = privateFolder.resolve(ApplicationProperties.getValue("private.filename", prvFile.getFileName().toString() + ".pub"));
			
			if(Files.exists(prvFile)) {
				LOG.info("Backing up existing private key file .. ");
				Files.move(prvFile, prvFile.getParent().resolve(prvFile.getFileName().toString() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
			}
			if(Files.exists(pubFile)) {
				LOG.info("Backing up existing public key file .. ");
				Files.move(pubFile, pubFile.getParent().resolve(pubFile.getFileName().toString() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
			}
			
			Files.write(prvFile, Base64.getDecoder().decode(clusterJoin.privateKey()));
			Files.write(pubFile, Base64.getDecoder().decode(clusterJoin.publicKey()));
			
		}
		
		
		/* Key server */
		if(StringUtils.isBlank(clusterJoin.keyserverHost())) {
			LOG.info("Removing key server configuration .. ");
			
			clusterProperties.remove("keyserver.host");
			clusterProperties.remove("keyserver.port");
			clusterProperties.remove("keyserver.path");
			clusterProperties.remove("keyserver.secret");
			clusterProperties.remove("keyserver.reference");
			clusterProperties.remove("keyserver.insecureSsl");
		}
		else {
			LOG.info("Updating key server configuration .. ");
			
			clusterProperties.setProperty("keyserver.host", clusterJoin.keyserverHost());
			clusterProperties.setProperty("keyserver.secret", clusterJoin.keyserverSecret());
			clusterProperties.setProperty("keyserver.reference", clusterJoin.keyserverReference());
			clusterProperties.setProperty("keyserver.insecureSsl", String.valueOf(clusterJoin.keyserverInsecureSsl()));
			if(clusterJoin.keyserverPort() > 0 && clusterJoin.keyserverPort() != 443) {
				clusterProperties.setProperty("keyserver.port", String.valueOf(clusterJoin.keyserverPort()));	
			}
			else {
				clusterProperties.remove("keyserver.port");
			}
			if(Objects.equals("/ks/api/secrets", clusterJoin.keyserverPath())) {
				clusterProperties.remove("keyserver.path");	
			}
			else {
				clusterProperties.setProperty("keyserver.path", clusterJoin.keyserverPath());
			}
			
		}
		
		/* Save cluster config */
		LOG.info("Saving cluster configuration .. ");

		try(var rdr = Files.newBufferedWriter(clusterPropertiesFile)) {
			clusterProperties.store(rdr, "Properties updated as result of cluster join");
		}

		LOG.info("Reconfigured for cluser ", clusterJoin.clusterName());
		
		throw new UriRedirect("/app/ui/" + JoinWithClusterPage.URI);
	}
}

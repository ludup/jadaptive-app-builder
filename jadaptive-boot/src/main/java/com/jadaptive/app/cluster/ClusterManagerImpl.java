package com.jadaptive.app.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jgroups.Address;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Service;

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
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;
import com.sshtools.gardensched.DistributedScheduledExecutor;

@Service
public class ClusterManagerImpl extends AbstractUUIDObjectServceImpl<ClusterNode> implements ClusterManager, StartupAware, Lifecycle {

	private static Logger LOG = LoggerFactory.getLogger(ClusterManagerImpl.class);

	@Autowired
	private DistributedScheduledExecutor executor;
	
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
	private App app;

	private String serverId;
	private Set<ClusterService> services;
	private String configuredHostname;
	private String hostname;

	@Override
	public void onApplicationStartup() {
		setupEventsProxy();
		configuredHostname = hostname = ApplicationProperties.getValue("ha.hostname", "");
		if (hostname.equals("")) {
			try {
				hostname = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				hostname = "localhost";
			}	
			LOG.warn("ha.hostname is not set, using a generated ha.hostname of " + hostname);
		}
		
		tenantService.asSystem(() -> {
			
			serverId = ApplicationProperties.getValue("ha.id","");
			if (serverId.equals("")) {
				serverId = UUID.nameUUIDFromBytes((hostname + ":"+ getPort()).getBytes()).toString();
				LOG.warn("ha.id is not set, using a generated ha.id of " + serverId);
			} 
		
			var thisNode = new ClusterNode();
			thisNode.setUuid(serverId);
			thisNode.setHostname(hostname);
			
			thisNode.setVersion(Optional.ofNullable(
				ApplicationServiceImpl.getInstance().getBean(VersionProvider.class)).map(VersionProvider::getVersion).
				orElse("Unknown"));
			
			thisNode.setGroupAddress(executor.address().toString());
			thisNode.setStatus(ClusterNodeStatus.ONLINE);
			
			services = new LinkedHashSet<>();
			for(var bean : app.getBeans(ClusterServiceProvider.class)) {
				services = bean.transform(services);
			}
			if(services == null)
				services = new LinkedHashSet<>();

			var port = getPort();
			services.add(new ClusterService(ClusterService.HTTPS_SERVICE, port));
			thisNode.setServices(new ArrayList<>(services));
			
			saveOrUpdate(thisNode);
			
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
		});
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
		return services;
	}
	
	public ClusterNode clusterNodeForAddress(String groupAddress) {
		return clusterNodes.searchObjects(ClusterNode.class,
				SearchField.eq("groupAddress", groupAddress)
			).stream().findFirst().orElseThrow(() -> new ObjectNotFoundException(groupAddress));
	}

	@Override
	public Element renderColumn(String column, AbstractObject obj, ObjectTemplate rowTemplate) {

		if (column.equals("uuid")) {
			var spn = Html.span(obj.getUuid());
			if(obj.getUuid().equals(getServerId())) {
				spn.addClass("fw-bolder");
			}
			return spn;
		}
		else {
			var node = getObjectByUUID(obj.getUuid());
			if (column.equals("groupAddress")) {
				var spn = Html.span(node.getGroupAddress());
				if(obj.getUuid().equals(getServerId())) {
					spn.addClass("fw-bolder");
				}
				return spn;
			}
			else if (column.equals("hostname")) {
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
						srvcol.text(s.getService());
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
				default:
					row.appendChild(Html.i("fa-solid", "fa-triangle-exclamation","text-danger"));
					break;
				}
				row.appendChild(Html.i18n(ClusterNode.RESOURCE_KEY, "clusterNode."+ node.getStatus().name()).addClass("ms-3"));
				return row;
			}
		}
		return Html.span("");
	}

	private void setupEventsProxy() {
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
	public boolean isLeader() {
		return executor.rank() == 0;
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
	public void start() {
	}

	@Override
	public void stop() {
		executor.close();
	}

	@Override
	public boolean isRunning() {
		return !executor.isShutdown();
	}

}

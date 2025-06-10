
package com.jadaptive.api.cluster;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jadaptive.api.app.App;
import com.jadaptive.api.app.ApplicationProperties;
import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.cluster.ClusterNode.ClusterNodeStatus;
import com.jadaptive.api.db.SearchField;
import com.jadaptive.api.db.SystemOnlyObjectDatabase;
import com.jadaptive.api.db.TransactionService;
import com.jadaptive.api.entity.AbstractObject;
import com.jadaptive.api.entity.AbstractUUIDObjectServceImpl;
import com.jadaptive.api.events.EventService;
import com.jadaptive.api.events.SystemEvent;
import com.jadaptive.api.permissions.PermissionService;
import com.jadaptive.api.template.ObjectTemplate;
import com.jadaptive.api.tenant.TenantService;
import com.jadaptive.api.ui.Html;
import com.jadaptive.api.user.User;
import com.jadaptive.api.user.UserService;

@Service
public class ClusterManagerImpl extends AbstractUUIDObjectServceImpl<ClusterNode> implements ClusterManager, StartupAware {

	private static final Duration HEARTBEAT = Duration.ofSeconds(Integer.parseInt(System.getProperty("ha.heartbeat", "5")));
	private static final Duration HEARTBEAT_GRACE = Duration.ofSeconds(Integer.parseInt(System.getProperty("ha.heartbeatGrace", "5")));
	private static final Duration DELETE_CLUSTER_EVENT = Duration.ofSeconds(Integer.parseInt(System.getProperty("ha.deleteClusterEvent", "10")));

	private static Logger LOG = LoggerFactory.getLogger(ClusterManagerImpl.class);
	

	@Autowired
	private SystemOnlyObjectDatabase<ClusterNode> clusterNodes;

	@Autowired
	private SystemOnlyObjectDatabase<ClusterEvent> clusterEvents;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private UserService userService;

	@Autowired
	private EventService eventService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private App app;

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private String serverId;
	private Map<String, ClusterNodeStatus> lastKnownStatus = Collections.synchronizedMap(new HashMap<>());
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

			thisNode.setStatus(ClusterNodeStatus.ONLINE);
			thisNode.setLastHeartbeat(nowInUTC().toEpochMilli());
			
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

			executor.execute(() -> {
				try {
					startLeaderElection();
				}
				catch(Exception e) {
					LOG.error("Election failed.", e);
				}
			});
			
			executor.scheduleWithFixedDelay(this::heartBeat, HEARTBEAT.toMillis(), HEARTBEAT.toMillis(), TimeUnit.MILLISECONDS);
			
			new Thread(() -> {
				tenantService.asSystem(() -> {
					clusterNodes.watch(ClusterNode.class, change -> {
						switch (change.type()) {
						case INSERT:
						case UPDATE:
						case REPLACE:
							var doc = change.document().get();
							var lastStatus = lastKnownStatus.getOrDefault(change.uuid(), ClusterNodeStatus.OFFLINE);
							var newStatus = doc.getStatus();
							
							if(lastStatus != newStatus) {
								nodeStatusChanged(doc, lastStatus, newStatus);
							}
							break;
						case DELETE:
							var deletedKey = change.uuid();
							LOG.info("Server with ID {} deleted from cluster nodes.", deletedKey);
							// Handle server offline
							var leader = isLeader();
							lastKnownStatus.remove(deletedKey);
							if(deletedKey.equals(serverId) || !leader)
								startLeaderElection();
							break;
						default:
							break;
						}
					});
				});
			}, "ClusterManagerMembershipMonitor").start();
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
			if (column.equals("lastHeartbeat")) {
				var spn = Html.span(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(node.getLastHeartbeat()), ZoneId.of("UTC"))));
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
				case LEADER:
					row.appendChild(Html.i("fa-solid", "fa-star", "text-success"));
					break;
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
				executor.execute(() -> {

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
					clusterEvents.saveOrUpdate(cevt);
					
					/* We fired the event, we delete after a short delay (hopefully all
					 * nodes have received by this time */
					executor.schedule(() -> {
						clusterEvents.delete(cevt);
					}, DELETE_CLUSTER_EVENT.toMillis(), TimeUnit.MILLISECONDS);	
				});
				
			}
		});
		
		
		/* Receiver. Waits for cluster events and re-fires */
		new Thread(() -> {
			tenantService.asSystem(() -> {
				clusterEvents.watch(ClusterEvent.class, change -> {
					switch (change.type()) {
					case INSERT:
						var doc = change.document().get();
						if(!doc.getClusterNode().equals(serverId)) {
							LOG.info("Received broadcast event {} as tenant {} and user {}", doc.getEvent().getResourceKey(), doc.getTenant(), doc.getUser());
							
							/* Re-fire, in context of original tenant and user */
							tenantService.executeAs(tenantService.getTenantByUUID(doc.getTenant()), () -> {
								User usr = permissionService.getSystemUser();
								if(!doc.getUser().equals(usr.getUuid())) {
									usr = userService.getObjectByUUID(doc.getUser());
								}
								
								/* Set to remote so it is not re-broadcast or audited */
								doc.getEvent().setRemote(true);
								
								permissionService.as(usr, () -> {
									eventService.publishEvent(doc.getEvent());
									return null;
								});
							});
						}
						break;
					default:
						break;
					}
				});
			});
		}, "ClusterManagerEventProxy").start();
	}

	private void nodeStatusChanged(ClusterNode document, ClusterNodeStatus lastStatus,
			ClusterNodeStatus newStatus) {
		LOG.info("Server with ID of {} status changed from {} to {}.", document.getUuid(), lastStatus, newStatus);

		var weWereLeader = isLeader();
		lastKnownStatus.put(document.getUuid(), newStatus);
		
		if(!document.getUuid().equals(serverId)) {
			if(lastStatus == ClusterNodeStatus.OFFLINE) {
				eventService.publishEvent(new ClusterNodeConnectedEvent(document));
			}

			if(newStatus == ClusterNodeStatus.OFFLINE) {
				eventService.publishEvent(new ClusterNodeDisconnectedEvent(document));
			}
		}

		
		if(!document.getUuid().equals(serverId) && newStatus == ClusterNodeStatus.LEADER && weWereLeader) {
			/* Somebody else is now the leader */
			var us = getObjectByUUID(serverId);
			us.setStatus(ClusterNodeStatus.ONLINE);
			saveOrUpdate(us);
			eventService.publishEvent(new LeadershipLostEvent(us));
		}
		else if(document.getUuid().equals(serverId)) {
			if(newStatus == ClusterNodeStatus.LEADER) {
				eventService.publishEvent(new LeadershipObtainedEvent(document));
			}
			else if(lastStatus == ClusterNodeStatus.LEADER) {
				eventService.publishEvent(new LeadershipLostEvent(document));
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
		return lastKnownStatus.computeIfAbsent(serverId, k -> getObjectByUUID(k).getStatus()) == ClusterNodeStatus.LEADER;
	}

	@Override
	protected Class<ClusterNode> getResourceClass() {
		return ClusterNode.class;
	}

	private void heartBeat() {
		try {
			tenantService.asSystem(() -> {
				transactionService.executeTransaction(() -> {
					var now = nowInUTC().toEpochMilli();
					var interval = HEARTBEAT.plus(HEARTBEAT_GRACE).toMillis() * 2;
					var expire = now - interval;
					
					/* TODO can we just update a single attribute via the abstraction? */
					var us = getObjectByUUID(serverId);
					us.setLastHeartbeat(now);
					saveOrUpdate(us);
					
					clusterNodes.list(ClusterNode.class,
						SearchField.and(
							SearchField.not("uuid", serverId),
							SearchField.not("status", ClusterNodeStatus.OFFLINE.name()),
							SearchField.lt("lastHeartbeat", expire)
						)
					).forEach(n -> {
						executor.execute(() -> nodeNowOffline(n));
					});
				});
			});
		}
		catch(Exception e) {
			LOG.error("Error during heartbeat.", e);
		}
	}
	
	private void nodeNowOffline(ClusterNode node) {
		tenantService.asSystem(() -> {
			var was = node.getStatus();
			LOG.info("Node {} ({}) was {}, is now offline.", node.getUuid(), node.getHostname(), was);
			node.setStatus(ClusterNodeStatus.OFFLINE);
			saveOrUpdate(node);
			if(was == ClusterNodeStatus.LEADER) {
				executor.execute(() -> {
					startLeaderElection();	
				});
			}
		});
	}
	
	private void startLeaderElection() {
		LOG.info("Starting election");
		heartBeat();
		tenantService.asSystem(() -> {
			do {
				transactionService.executeTransaction(() -> {
					try {
						try {
							Thread.sleep(1000 + (int)( Math.random() * 1000));
						} catch (InterruptedException e) {
							return;
						}
						
						var obj = clusterNodes.get(getServerId(), getResourceClass());
						obj.setStatus(ClusterNodeStatus.LEADER);
						saveOrUpdate(obj);
						return;
					}
					catch(Exception e) {
					}

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e2) {
						return;
					}
				});
			
			} while (!isLeader());
		});
	}



	private int getPort() {
		return Integer.parseInt(
			ApplicationProperties.getValue("ha.port", 
				ApplicationProperties.getValue("server.port", "443")
			)
		);
	}

	private static Instant nowInUTC() {
		return ZonedDateTime.now(ZoneId.of("UTC")).toInstant();
	}
}

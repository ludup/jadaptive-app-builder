package com.jadaptive.plugins.sshd;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.jadaptive.api.app.StartupAware;
import com.jadaptive.api.cluster.ClusterManager;
import com.jadaptive.api.cluster.ClusterNodeConnectedEvent;
import com.jadaptive.api.cluster.ClusterNodeDisconnectedEvent;
import com.jadaptive.api.events.EventService;
import com.sshtools.common.ssh.GlobalRequest;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.util.ByteArrayWriter;
import com.sshtools.server.SshServerContext;
import com.sshtools.synergy.nio.ProtocolContext;

public abstract class AbstractClusterInterfaceFactory<T extends ProtocolContext,S extends SSHInterface> implements SSHInterfaceFactory<T, S>, StartupAware {

	/* TODO change actual message name too */
	public static final String CLUSTER_SERVERS = "cluster-nodes@jadaptive.com";
	
	@Autowired
	private EventService eventService; 
	@Autowired
	private ClusterManager clusterManager;

	private SshServerContext ctx;

	
	@Override
	public final void onApplicationStartup() {
		eventService.on(ClusterNodeConnectedEvent.class, e -> clusterServerChanged());
		eventService.on(ClusterNodeDisconnectedEvent.class, e -> clusterServerChanged());
	}

	public final void clusterServerChanged() {
		if(ctx != null) {
			var cm = ctx.getConnectionManager();
			cm.getAllConnections().forEach(cnx -> {
				sendClusterServersToConnection(cnx);
			});
		}
	}

	public final void sendClusterServersToConnection(SshConnection cnx) {
		try(var wtr = new ByteArrayWriter()) {
			var nodes = clusterManager.streamAll().toList();
			wtr.write(nodes.size());
			nodes.forEach(node -> {
				try {
					wtr.writeString(node.getUuid());
					wtr.writeString(node.getStatus().name());
					wtr.writeUINT64(0);
					wtr.writeString(node.getHostname());
					var srvs = node.getServices();
					wtr.write(srvs.size());
					srvs.forEach(srv -> {
						try {
							wtr.writeString(srv.getService());
							wtr.writeInt(srv.getPort());
							wtr.writeString(srv.getPublicKey());
						}
						catch(IOException ioe) {
							throw new UncheckedIOException(ioe);
						}
					});
				}
				catch(IOException ioe) {
					throw new UncheckedIOException(ioe);
				}
			});
			
			cnx.sendGlobalRequest(new GlobalRequest(CLUSTER_SERVERS, cnx, wtr.toByteArray()));
		}
		catch(IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}
}
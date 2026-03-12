package com.jadaptive.api.cluster;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.template.DynamicColumnService;
import com.sshtools.gardensched.DistributedMachine;

public interface ClusterManager extends Closeable, AbstractUUIDObjectService<ClusterNode>, DynamicColumnService {
	
	String getServerId();

	Set<ClusterService> getServices();

	Optional<String> getExternalHostname();

	String getHostname();

	ClusterNode getThisNode();

	ClusterNode getObjectByGroupName(String groupName);

	void initCluster();

	void setupCluster(DistributedMachine machine);

	void join(String token, String refreshToken, String address, boolean insecureSsl) throws IOException, InterruptedException;

	boolean isJoined();

	List<String> getInitialNodes();

	void addInitialNode(String peerIpAddress) throws IOException;
}

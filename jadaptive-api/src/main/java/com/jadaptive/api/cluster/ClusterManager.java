package com.jadaptive.api.cluster;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.Optional;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.template.DynamicColumnService;
import com.sshtools.gardensched.DistributedScheduledExecutor;

public interface ClusterManager extends Closeable, AbstractUUIDObjectService<ClusterNode>, DynamicColumnService {

	String getServerId();

	Set<ClusterService> getServices();

	Optional<String> getExternalHostname();

	String getHostname();

	ClusterNode getThisNode();

	ClusterNode getObjectByGroupName(String groupName);

	void initCluster();

	void setupCluster(DistributedScheduledExecutor executor);

	void join(String token, String refreshToken, String address) throws IOException, InterruptedException;

	boolean isJoined();
}

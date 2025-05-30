package com.jadaptive.api.cluster;

import java.io.Closeable;
import java.util.Set;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.template.DynamicColumnService;

public interface ClusterManager extends Closeable, AbstractUUIDObjectService<ClusterNode>, DynamicColumnService {

	String getServerId();

	Set<ClusterService> getServices();
	
	boolean runOnceOnCluster(String taskName, Runnable r);

	void queueTask(Runnable task);

	boolean isLeader();
}
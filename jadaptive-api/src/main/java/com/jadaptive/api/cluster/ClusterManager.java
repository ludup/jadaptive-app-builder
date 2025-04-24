package com.jadaptive.api.cluster;

import java.io.Closeable;
import java.util.Set;

import com.jadaptive.api.entity.AbstractUUIDObjectService;
import com.jadaptive.api.template.DynamicColumnService;

public interface ClusterManager extends Closeable, AbstractUUIDObjectService<ClusterNode>, DynamicColumnService {

	String getServerId();

	boolean isLeader();

	Set<ClusterService> getServices();

}
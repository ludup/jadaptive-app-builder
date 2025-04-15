package com.jadaptive.api.cluster;

import java.util.Set;

public interface ClusterServiceProvider {

	Set<ClusterService> transform(Set<ClusterService> service);
}
